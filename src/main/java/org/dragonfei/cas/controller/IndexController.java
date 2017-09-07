package org.dragonfei.cas.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.RolesAllowed;

/**
 * Created by longfei on 17-9-7.
 */
@RestController
@RolesAllowed("ROLE_ADMIN")
public class IndexController {

    @GetMapping("/")
    public String home(){
        return "this is my test";
    }
}
