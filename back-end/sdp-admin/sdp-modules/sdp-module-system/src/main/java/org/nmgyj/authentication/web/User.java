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

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Spring MVC 演示用的简单用户模型（非业务 {@code SysUser}）。
 *
 * @author nmgyj
 * @since 2026-05-10
 */
@Schema(description = "演示用户")
public class User {

    @Schema(description = "姓名", example = "theonefx")
    private String name;

    @Schema(description = "年龄", example = "666")
    private Integer age;

    /**
     * @return 姓名
     */
    public String getName() {
        return name;
    }

    /**
     * @param name 姓名
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return 年龄
     */
    public Integer getAge() {
        return age;
    }

    /**
     * @param age 年龄
     */
    public void setAge(Integer age) {
        this.age = age;
    }
}
