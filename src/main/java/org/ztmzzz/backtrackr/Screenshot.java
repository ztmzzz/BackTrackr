package org.ztmzzz.backtrackr;


import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.MultiResolutionImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.sun.jna.platform.WindowUtils;
import lombok.Data;
import org.springframework.stereotype.Component;


import javax.imageio.ImageIO;

@Component
public class Screenshot {
    private static final String screenshotPath = "screenshot/";
    private static final List<String> blockWindowNames = new ArrayList<>(List.of("KeePassXC"));

    private final List<WindowInfo> allWindows = new ArrayList<>();

    static class WindowInfo {
        String title;
        Rectangle locAndSize;
        int depth;

        boolean isBlock;

        public WindowInfo(String title, Rectangle locAndSize, int depth, boolean isBlock) {
            this.title = title;
            this.locAndSize = locAndSize;
            this.depth = depth;
            this.isBlock = isBlock;
        }
    }


    static class ScreenshotInfo {
        String windowName;
        BufferedImage image;

        public ScreenshotInfo(String windowName, BufferedImage image) {
            this.windowName = windowName;
            this.image = image;
        }
    }

    public static void main(String[] args) throws AWTException, IOException {
        Screenshot screenshot = new Screenshot();
        screenshot.screenshot();
    }

    public ScreenshotInfo screenshot() throws AWTException, IOException {
        // 获取当前屏幕上所有窗口
        AtomicInteger depth = new AtomicInteger();
        WindowUtils.getAllWindows(true).forEach(desktopWindow -> {
            if (desktopWindow.getTitle().equals("")) {
                return;
            }
            for (String disableWindowName : blockWindowNames) {
                if (desktopWindow.getTitle().contains(disableWindowName)) {
                    allWindows.add(new WindowInfo(desktopWindow.getTitle(), desktopWindow.getLocAndSize(), depth.getAndIncrement(), true));
                    return;
                }
            }
            allWindows.add(new WindowInfo(desktopWindow.getTitle(), desktopWindow.getLocAndSize(), depth.getAndIncrement(), false));
        });
        Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
        Dimension scaledScreenSize = defaultToolkit.getScreenSize();
        GraphicsDevice graphDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        DisplayMode disMode = graphDevice.getDisplayMode();
        int screenWidth = disMode.getWidth();
        int screenHeight = disMode.getHeight();
        Robot robot = new Robot();
        MultiResolutionImage multiResolutionImage = robot.createMultiResolutionScreenCapture(new Rectangle(0, 0, scaledScreenSize.width, scaledScreenSize.height));
        Image image = multiResolutionImage.getResolutionVariant(screenWidth, screenHeight);
        BufferedImage screenCapture = (BufferedImage) image;

        BufferedImage maskImage = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D maskGraphics = maskImage.createGraphics();
        allWindows.sort((o1, o2) -> o2.depth - o1.depth);
        for (WindowInfo window : allWindows) {
            if (window.isBlock) {
                maskGraphics.setComposite(AlphaComposite.SrcOver);
                maskGraphics.setColor(Color.BLACK);
                maskGraphics.fillRect(window.locAndSize.x, window.locAndSize.y, window.locAndSize.width, window.locAndSize.height);
            } else {
                maskGraphics.setComposite(AlphaComposite.Clear);
                maskGraphics.fillRect(window.locAndSize.x, window.locAndSize.y, window.locAndSize.width, window.locAndSize.height);
            }
        }
        maskGraphics.dispose();
//        ImageIO.write(maskImage, "png", new File(screenshotPath + "mask.png"));

        Graphics2D resultGraphics = screenCapture.createGraphics();
        resultGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 1f));
        resultGraphics.drawImage(maskImage, 0, 0, null);
        resultGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
        resultGraphics.dispose();

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
        String formattedDateTime = now.format(formatter); // "2023-06-18 12:34:56"
        ImageIO.write(screenCapture, "jpg", new File(screenshotPath + formattedDateTime + ".jpg"));

        String frontWindowName = allWindows.get(allWindows.size() - 1).title;
        ScreenshotInfo info = new ScreenshotInfo(frontWindowName, screenCapture);
        return info;
    }
}
