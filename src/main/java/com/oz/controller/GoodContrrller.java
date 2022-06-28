package com.oz.controller;

import com.oz.entity.dto.GoodDTO;
import com.oz.service.GoodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/good")
public class GoodContrrller {
    @Autowired
    GoodService goodService;

    @GetMapping(value = "/buildGood/{param}")
    public String buildGood(@PathVariable("param") String param) {
        if (param == null) {
            return "参数错误";
        }
        String[] params = param.split(",");
        if (params.length != 4) {
            return "参数错误";
        }
        GoodDTO goodDTO = new GoodDTO();
        goodDTO.setTime(Integer.valueOf(params[0]));
        goodDTO.setGoodId(params[1]);
        goodDTO.setUsername(params[2]);
        goodDTO.setPassword(params[3]);
        return  buyGood(goodDTO);
    }

    @PostMapping(value = "/buyGood")
    public String buyGood(@RequestBody GoodDTO goodDTO) {
        int count = 0;
        for (int i = 0; i < goodDTO.getTime(); i++) {
            try {
                boolean isok = goodService.buyGood(goodDTO.getGoodId(), goodDTO.getUsername(), goodDTO.getPassword());
                if (isok) {
                    count++;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return "本次成功砍价次数为:" + count;
    }
}
