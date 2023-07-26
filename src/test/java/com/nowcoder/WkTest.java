package com.nowcoder;

import java.io.IOException;

/**
 * @Author Xiao Guo
 * @Date 2023/5/19
 */

public class WkTest {

    public static void main(String[] args) {
        // 需要书写完整路径
        // cmd:（wkhtmltoimage.exe所在路径，图片质量，访问的html的地址，html转换为图片后存入的位置和图片名称）
        // 》d:/soft/wkhtmltopdf/bin/wkhtmltoimage --quality 75 https://wkhtmltopdf.org/downloads.html d:/codeJava/workspace3/wk-images/eea6709a9eba40a2aaaf957571e11594.png
        String cmd = "d:/soft/wkhtmltopdf/bin/wkhtmltoimage --quality 75  https://wkhtmltopdf.org/downloads.html d:/codeJava/workspace3/wk-images/3.png";

        try {
            // 先输出ok，操作系统异步输出图片信息（并发）。
            Runtime.getRuntime().exec(cmd);
            System.out.println("ok!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
