package com.hj.supercoupon.merchant.admin.task;

import com.github.javafaker.Address;
import com.github.javafaker.Faker;
import com.github.javafaker.PhoneNumber;
import org.junit.jupiter.api.Test;

import java.util.Locale;

/**
 * Faker 单元测试类
 * @Author web-cat
 */
public class FakerTests {
    @Test
    public void testFaker() {
        // 创建一个 Faker 实例
        Faker faker = new Faker(Locale.CHINA);

        // 生成中文名
        String chineseName = faker.name().fullName();
        System.out.println("中文名: " + chineseName);

        // 生成手机号
        PhoneNumber phoneNumber = faker.phoneNumber();
        String mobileNumber = phoneNumber.cellPhone();
        System.out.println("手机号: " + mobileNumber);

        // 生成电子邮箱
        String email = faker.internet().emailAddress();
        System.out.println("电子邮箱: " + email);

        // 生成省市区县的住址
        Address address = faker.address();
        String fullAddress = address.country() + " " + address.state() + " " + address.city() + " " + address.streetAddress();
        System.out.println("住址: " + fullAddress);
    }
}
