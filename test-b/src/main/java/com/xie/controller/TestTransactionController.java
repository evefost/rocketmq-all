package com.xie.controller;


import com.xie.buz.ServiceAServiceImpl;
import com.xie.producer.InterFaceA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestTransactionController {

    @Autowired
    private ServiceAServiceImpl transactionService;

    @Autowired(required = false)
    private InterFaceA interFaceA;

    int count =0;
    @RequestMapping(value = "/addUser", method = RequestMethod.GET)
    public String addUser() {
        count++;
        transactionService.addUser(count);
        return "addUser:"+count;
    }

    @RequestMapping(value = "/addUser2", method = RequestMethod.GET)
    public String addUser2() {
        count++;
        transactionService.addUser2(count);
        return "addUser2:"+count;
    }


    @RequestMapping(value = "/noTag", method = RequestMethod.GET)
    public String noTag() {
        count++;
        transactionService.noTag(count);
        return "noTag:"+count;
    }

}