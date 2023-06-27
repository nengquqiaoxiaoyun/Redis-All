package cn.huakai.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author: huakaimay
 * @since: 2023-06-19
 */
@RestController
@RequestMapping("/hello")
public class HelloController {


    @GetMapping
    public String hello(String name) {
        String s = new String();
        return "hello" + name;
    }

    public static void main(String[] args) {
        List<Integer> list = IntStream.rangeClosed(0, 19)
                .boxed()
                .collect(Collectors.toList());


        int index = getIndex(list, -1);
        System.out.println("list = " + list);
        System.out.println("index = " + index);
    }

    public static int getIndex(List<Integer> list, Integer target) {
        int left = 0;
        int right = list.size() - 1;

        while (left <= right) {
            int mid = (left + right) / 2;

            if (list.get(mid) == target)
                return mid;

            if (list.get(mid) >= target) {
                right = mid -1;
            } else {
                left = mid + 1;
            }
        }

        return -1;
    }

}
