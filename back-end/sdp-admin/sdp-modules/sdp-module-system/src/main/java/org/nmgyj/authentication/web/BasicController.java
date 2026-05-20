/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.nmgyj.authentication.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Spring MVC 基础绑定与视图跳转演示控制器。
 *
 * @author nmgyj
 * @since 2026-05-10
 */
@Controller
public class BasicController {

    /**
     * 问候接口示例。
     *
     * @param name 查询参数 {@code name}
     * @return 问候语文本
     */
    @RequestMapping("/hello")
    @ResponseBody
    public String hello(@RequestParam(name = "name", defaultValue = "unknown user") String name) {
        return "Hello " + name;
    }

    /**
     * 返回演示 {@link User} 对象。
     *
     * @return JSON 用户对象
     */
    @RequestMapping("/user")
    @ResponseBody
    public User user() {
        User user = new User();
        user.setName("theonefx");
        user.setAge(666);
        return user;
    }

    /**
     * 演示模型绑定保存用户。
     *
     * @param u 绑定后的用户参数
     * @return 描述即将保存的数据
     */
    @RequestMapping("/save_user")
    @ResponseBody
    public String saveUser(User u) {
        return "user will save: name=" + u.getName() + ", age=" + u.getAge();
    }

    /**
     * 转发到静态页 {@code index.html}。
     *
     * @return 视图名
     */
    @RequestMapping("/html")
    public String html() {
        return "index.html";
    }

    /**
     * 演示 {@link ModelAttribute} 预填充模型。
     *
     * @param name 请求参数 name（演示未直接使用）
     * @param age  请求参数 age（演示未直接使用）
     * @param user 模型中的 User（将被写入固定演示数据）
     */
    @ModelAttribute
    public void parseUser(@RequestParam(name = "name", defaultValue = "unknown user") String name,
                          @RequestParam(name = "age", defaultValue = "12") Integer age,
                          User user) {
        user.setName("zhangsan");
        user.setAge(18);
    }

    /**
     * 另一问候示例。
     *
     * @param Uname 查询参数 {@code Uname}
     * @return 问候语文本
     */
    @RequestMapping("/loginapi")
    @ResponseBody
    public String login(@RequestParam(name = "Uname", defaultValue = "unknown user") String Uname) {
        return "Hello " + Uname;
    }
}
