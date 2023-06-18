package org.ztmzzz.backtrackr;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class OCR {
    String venvPath = "./venv";
    String venvPythonPath = "./venv/Scripts/python";
    String venvPipPath = "./venv/Scripts/pip3";
    String hubPath = "./venv/Scripts/hub";

    public static void main(String[] args) throws IOException, InterruptedException {
        OCR ocr = new OCR();
        String a = ocr.getAllText(ImageIO.read(new File("screenshot/raw.jpg")));
        System.out.println(a);
    }

    public String getAllText(BufferedImage image) throws IOException {
        String base64Image = imageToBase64(image);
        String requestBody = "{\"images\":[\"" + base64Image + "\"]}";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        // 发送POST请求并获取响应
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(
                "http://127.0.0.1:8866/predict/ch_pp-ocrv3",
                requestEntity,
                String.class
        );

        // 解析响应结果
        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new RuntimeException("OCR API request failed");
        }
    }

    private String imageToBase64(BufferedImage image) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.getEncoder().encodeToString(byteArray);
    }

    public void installService() throws IOException, InterruptedException {
        String pythonPath = getPythonPath();

        if (new File(venvPath).exists()) {
            return;
        }

        ProcessBuilder pb = new ProcessBuilder(pythonPath, "-m", "venv", venvPath);
        Process p = pb.start();
        p.waitFor();
        if (p.exitValue() != 0) {
            throw new IOException("创建虚拟环境失败");
        }
        pb = new ProcessBuilder(venvPipPath, "install", "paddlehub paddlepaddle shapely pyclipper");
        p = pb.start();
//        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
//        String line;
//        while ((line = in.readLine()) != null) {
//            System.out.println(line);
//        }
        p.waitFor();
        if (p.exitValue() != 0) {
            throw new IOException("安装paddle失败");
        }
        pb = new ProcessBuilder(hubPath, "install", "ch_pp-ocrv3_det==1.1.0");
        p = pb.start();
        p.waitFor();
        if (p.exitValue() != 0) {
            throw new IOException("安装ch_pp-ocrv3_det失败");
        }
        pb = new ProcessBuilder(hubPath, "install", "ch_pp-ocrv3==1.2.0");
        p = pb.start();
        p.waitFor();
        if (p.exitValue() != 0) {
            throw new IOException("安装ch_pp-ocrv3失败");
        }
    }

    public void startService() {
        final Object lock = new Object();
        final AtomicBoolean isServiceStarted = new AtomicBoolean(false);

        Thread thread = new Thread(() -> {
            try {
                ProcessBuilder pb = new ProcessBuilder(hubPath, "serving", "start", "-m", "ch_pp-ocrv3");
                Process p = pb.start();
                BufferedReader in = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.contains("Running on")) {
                        isServiceStarted.set(true);
                        break;
                    }
                }
                in.close();
                synchronized (lock) {
                    lock.notify();
                }
                p.waitFor();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        thread.start();
        long timeout = 20000;
        long startTime = System.currentTimeMillis();
        synchronized (lock) {
            while (!isServiceStarted.get()) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                long remainingTime = timeout - elapsedTime;
                if (remainingTime <= 0) {
                    break;
                }
                try {
                    lock.wait(remainingTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("OCR服务启动成功");
    }

    private String getPythonPath() throws IOException {
        ProcessBuilder pb = new ProcessBuilder("where", "python");
        Process p = pb.start();
        // 获取进程的输出
        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = in.readLine();
        in.close();
        if (line == null) {
            throw new IOException("未找到python");
        } else {
            p.destroy();
            return line;
        }
    }
}
