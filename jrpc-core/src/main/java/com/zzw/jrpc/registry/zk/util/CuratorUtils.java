package com.zzw.jrpc.registry.zk.util;

import com.zzw.jrpc.base.enums.RpcConfigEnum;
import com.zzw.jrpc.base.utils.PropertiesFileUtil;
import com.zzw.jrpc.loadbalance.LoadBalancer;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public class CuratorUtils {

    private static final int BASE_SLEEP_TIME = 1000;
    private static final int MAX_RETRIES = 3;
    public static final String ZK_REGISTER_ROOT_PATH = "/zzw-jrpc";
    private static final Map<String, List<String>> SERVICE_ADDRESS_MAP = new ConcurrentHashMap<>();
    private static final Set<String> REGISTERED_PATH_SET = ConcurrentHashMap.newKeySet();
    private static CuratorFramework zkClient;
    private static final String DEFAULT_ZK_ADDRESS = "localhost:2181";

    private static void registerWatcher(String rpcServiceName, CuratorFramework zkClient, LoadBalancer balancer) throws Exception {
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName;
        PathChildrenCache cache = new PathChildrenCache(zkClient, servicePath, true);
        cache.getListenable().addListener((curatorFramework, pathChildrenCacheEvent) -> {
            List<String> serviceAddress = curatorFramework.getChildren().forPath(servicePath);
            SERVICE_ADDRESS_MAP.put(rpcServiceName, serviceAddress);
            balancer.updateNodeList(serviceAddress);
        });
        cache.start();
    }

    public static CuratorFramework getZkClient() {
        Properties properties = PropertiesFileUtil.readPropertiesFile(RpcConfigEnum.JRPC_CONFIG_PATH.getPropertyValue());
        String zkAddress = DEFAULT_ZK_ADDRESS;

        if (properties != null) {
            zkAddress = properties.getProperty(RpcConfigEnum.ZK_ADDRESS.getPropertyValue(), DEFAULT_ZK_ADDRESS);
        }
        if (zkClient != null && zkClient.getState() == CuratorFrameworkState.STARTED) {
            return zkClient;
        }
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RETRIES);
        zkClient = CuratorFrameworkFactory.builder()
                .connectString(zkAddress)
                .retryPolicy(retryPolicy)
                .build();
        zkClient.start();
        try {
            if (!zkClient.blockUntilConnected(30, TimeUnit.SECONDS)) {
                throw new RuntimeException("Timeout waiting to connect to Zookeeper!");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return zkClient;
    }


    public static List<String> getChildNodes(CuratorFramework zkClient, String rpcServiceName, LoadBalancer balancer) {
        if (SERVICE_ADDRESS_MAP.containsKey(rpcServiceName)) {
            return SERVICE_ADDRESS_MAP.get(rpcServiceName);
        }
        List<String> nodeList = new ArrayList<>();
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName;
        try {
            nodeList = zkClient.getChildren().forPath(servicePath);
            SERVICE_ADDRESS_MAP.put(rpcServiceName, nodeList);
            balancer.updateNodeList(nodeList);
            registerWatcher(rpcServiceName, zkClient, balancer);
        } catch (Exception e) {
            log.error("get child nodes for path [{}] fail", servicePath);
        }
        return nodeList;
    }

    public static void createTemporaryNode(CuratorFramework zkClient, String path) {
        createNode(zkClient, path, CreateMode.EPHEMERAL);
    }

    public static void createPersistentNode(CuratorFramework zkClient, String path) {
        createNode(zkClient, path, CreateMode.PERSISTENT);
    }

    private static void createNode(CuratorFramework zkClient, String path, CreateMode mode) {
        try {
            if (REGISTERED_PATH_SET.contains(path) || zkClient.checkExists().forPath(path) != null) {
                log.info("The node already existed. The node path is: [{}]", path);
            } else {
                zkClient.create().creatingParentsIfNeeded().withMode(mode).forPath(path);
                log.info("The node is created successfully. The node is: [{}]", path);
            }
            REGISTERED_PATH_SET.add(path);
        } catch (Exception e) {
            log.error("create persistent node for path [{}] fail: [{}]", path, e.getMessage());
        }
    }

    public static void clearRegistry(CuratorFramework zkClient, InetSocketAddress address) {
        REGISTERED_PATH_SET.stream().parallel().forEach(path -> {
            try {
                if (path.endsWith(address.toString())) {
                    zkClient.delete().forPath(path);
                }
            } catch (Exception e) {
                log.error("clear registry for path [{}] fail.", path);
            }
        });
        REGISTERED_PATH_SET.clear();
        log.info("All registered services on server [{}] are cleared: [{}]",
                address, REGISTERED_PATH_SET);
    }

    private CuratorUtils() {
    }

}
