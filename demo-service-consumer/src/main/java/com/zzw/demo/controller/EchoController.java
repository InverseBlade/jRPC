package com.zzw.demo.controller;

import com.zzw.demo.service.EchoService;
import com.zzw.jrpc.annotation.JRpcReference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/echo")
public class EchoController {

    @JRpcReference
    EchoService echoService;

    @RequestMapping("/echo")
    @ResponseBody
    public String echo(@RequestParam("msg") String msg) {
        if (msg == null) msg = "";
        return echoService.hello(msg);
    }

}
