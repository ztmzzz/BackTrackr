package org.ztmzzz.backtrackr;


import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.MultiResolutionImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.sun.jna.platform.WindowUtils;


import javax.imageio.ImageIO;

public class Screenshot {
    private static final String screenshotPath = "screenshot/";
    private static final List<String> blockWindowNames = new ArrayList<>(List.of("KeePassXC"));

    private final List<WindowInfo> blockWindows = new ArrayList<>();
    private final List<WindowInfo> unblockWindows = new ArrayList<>();

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

    public static void main(String[] args) throws AWTException, IOException {
        Screenshot screenshot = new Screenshot();
        screenshot.screenshot();
    }

    private void screenshot() throws AWTException, IOException {
        // 获取当前屏幕上所有窗口
        AtomicInteger depth = new AtomicInteger();
        WindowUtils.getAllWindows(true).forEach(desktopWindow -> {
            if (desktopWindow.getTitle().equals("")) {
                return;
            }
            for (String disableWindowName : blockWindowNames) {
                if (desktopWindow.getTitle().contains(disableWindowName)) {
                    blockWindows.add(new WindowInfo(desktopWindow.getTitle(), desktopWindow.getLocAndSize(), depth.getAndIncrement(), true));
                    return;
                }
            }
            unblockWindows.add(new WindowInfo(desktopWindow.getTitle(), desktopWindow.getLocAndSize(), depth.getAndIncrement(), false));
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

//        BufferedImage screenCapture = robot.createScreenCapture(screenSize);

        ImageIO.write(screenCapture, "jpg", new File(screenshotPath + "raw.jpg"));

        BufferedImage maskImage = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D maskGraphics = maskImage.createGraphics();
        List<WindowInfo> allWindows = new ArrayList<>(blockWindows);
        allWindows.addAll(unblockWindows);
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
        ImageIO.write(maskImage, "png", new File(screenshotPath + "mask.png"));

        Graphics2D resultGraphics = screenCapture.createGraphics();
        resultGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 1f));
        resultGraphics.drawImage(maskImage, 0, 0, null);
        resultGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
        resultGraphics.dispose();

        ImageIO.write(screenCapture, "jpg", new File(screenshotPath + "screenshot.jpg"));
    }
}
