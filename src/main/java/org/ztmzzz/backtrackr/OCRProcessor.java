package org.ztmzzz.backtrackr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.ztmzzz.backtrackr.ImageProcessor.imageToBase64;

@Component
public class OCRProcessor {
    String venvPath = "./venv";
    String venvPythonPath = "./venv/Scripts/python";
    String venvPipPath = "./venv/Scripts/pip3";
    String hubPath = "C:/Users/ztmzzz/anaconda3/envs/paddle/Scripts/hub";
    private AtomicBoolean serviceStarted = new AtomicBoolean(false);
    private static final Logger logger = LoggerFactory.getLogger(OCRProcessor.class);

    public static void main(String[] args) throws IOException, InterruptedException {
        OCRProcessor ocrProcessor = new OCRProcessor();
        ocrProcessor.startService();
//        String a = ocrProcessor.getAllText(ImageIO.read(new File("screenshot/raw.jpg")));
//        System.out.println(a);
    }

    public String ocr(BufferedImage image) throws IOException {
        String base64Image = imageToBase64(image);
        return ocr(base64Image);
    }

    public String ocr(String base64Image) throws IOException {
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
            throw new RuntimeException("OCRProcessor API request failed");
        }
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
        final AtomicReference<Process> processRef = new AtomicReference<>();

        Thread thread = new Thread(() -> {
            try {
                ProcessBuilder pb = new ProcessBuilder(hubPath, "serving", "start", "-m", "ch_pp-ocrv3", "--use_gpu");
                Map<String, String> env = pb.environment();
                String oldPath = env.get("PATH");
                String cudaPath = "C:\\Program Files\\NVIDIA GPU Computing Toolkit\\CUDA\\v11.7\\bin;";
                env.put("PATH", cudaPath + oldPath);
                Process p = pb.start();
                processRef.set(p);
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

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Process p = processRef.get();
            if (p != null && p.isAlive()) {
//                p.destroyForcibly();
                try {
                    stopService();
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }));

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

        serviceStarted.set(isServiceStarted.get());
        if (serviceStarted.get()) {
            logger.info("OCR服务启动成功");
        } else {
            logger.error("OCR服务启动失败");
        }
    }

    public void stopService() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(hubPath, "serving", "stop");
        Process p = pb.start();
        p.waitFor();
    }

    public boolean isServiceStarted() {
        return serviceStarted.get();
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
