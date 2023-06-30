package org.ztmzzz.backtrackr;


import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.MultiResolutionImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;

import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.sun.jna.Native;
import com.sun.jna.platform.WindowUtils;
import com.sun.jna.platform.win32.*;
import com.sun.jna.ptr.IntByReference;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


import javax.imageio.ImageIO;

@Component
@Scope("prototype")
public class ScreenshotProcessor {
    public static final String screenshotPath = "screenshot/";
    private static final List<String> blockWindowNames = new ArrayList<>(List.of("KeePassXC"));

    private final List<WindowInfo> allWindows = new ArrayList<>();

    static class WindowInfo {
        String title;
        String processName;
        Rectangle locAndSize;
        int depth;

        boolean isBlock;

        public WindowInfo(String title, String processName, Rectangle locAndSize, int depth, boolean isBlock) {
            this.title = title;
            this.processName = processName;
            this.locAndSize = locAndSize;
            this.depth = depth;
            this.isBlock = isBlock;
        }
    }


    static class ScreenshotInfo {
        String frontWindowTitle;
        String processName;
        Timestamp timestamp;
        BufferedImage image;

        public ScreenshotInfo(String frontWindowTitle, String processName, Timestamp timestamp, BufferedImage image) {
            this.frontWindowTitle = frontWindowTitle;
            this.processName = processName;
            this.timestamp = timestamp;
            this.image = image;
        }
    }

    public static void main(String[] args) throws AWTException, IOException {
        ScreenshotProcessor screenshotProcessor = new ScreenshotProcessor();
        screenshotProcessor.screenshot();
    }

    public ScreenshotInfo screenshot() throws AWTException, IOException {
        // 获取当前屏幕上所有窗口
        AtomicInteger depth = new AtomicInteger();
        WindowUtils.getAllWindows(true).forEach(desktopWindow -> {
            if (desktopWindow.getTitle().equals("")) {
                return;
            }
            WinDef.HWND hwnd = desktopWindow.getHWND();
            String processName = getProcessName(hwnd);
            for (String disableWindowName : blockWindowNames) {
                if (desktopWindow.getTitle().contains(disableWindowName)) {
                    allWindows.add(new WindowInfo(desktopWindow.getTitle(), processName, desktopWindow.getLocAndSize(), depth.getAndIncrement(), true));
                    return;
                }
            }
            allWindows.add(new WindowInfo(desktopWindow.getTitle(), processName, desktopWindow.getLocAndSize(), depth.getAndIncrement(), false));
        });
        // 获得未缩放的截屏
        Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
        Dimension scaledScreenSize = defaultToolkit.getScreenSize();
        GraphicsDevice graphDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        DisplayMode disMode = graphDevice.getDisplayMode();
        int screenWidth = disMode.getWidth();
        int screenHeight = disMode.getHeight();
        Robot robot = new Robot();
        MultiResolutionImage multiResolutionImage = robot.createMultiResolutionScreenCapture(new Rectangle(0, 0, scaledScreenSize.width, scaledScreenSize.height));
        BufferedImage screenCapture = (BufferedImage) multiResolutionImage.getResolutionVariant(screenWidth, screenHeight);
        // 制作遮罩隐藏黑名单程序
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
        // 合并截屏和遮罩
        Graphics2D resultGraphics = screenCapture.createGraphics();
        resultGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 1f));
        resultGraphics.drawImage(maskImage, 0, 0, null);
        resultGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
        resultGraphics.dispose();
        // 分文件夹保存截屏
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        Path path = Paths.get(timestampToHourPath(timestamp));

        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }

        ImageIO.write(screenCapture, "jpg", new File(timestampToFullPath(timestamp)));

        String frontWindowTitle = allWindows.get(allWindows.size() - 1).title;
        String processName = allWindows.get(allWindows.size() - 1).processName;
        return new ScreenshotInfo(frontWindowTitle, processName, timestamp, screenCapture);
    }

    public static String timestampToHourPath(Timestamp timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp.getTime());
        String year = String.valueOf(cal.get(Calendar.YEAR));
        String month = String.format("%02d", cal.get(Calendar.MONTH) + 1);
        String day = String.format("%02d", cal.get(Calendar.DAY_OF_MONTH));
        String hour = String.format("%02d", cal.get(Calendar.HOUR_OF_DAY));
        return screenshotPath + year + "/" + month + "/" + day + "/" + hour + "/";
    }

    public static String timestampToFullPath(Timestamp timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp.getTime());
        String minute = String.format("%02d", cal.get(Calendar.MINUTE));
        String second = String.format("%02d", cal.get(Calendar.SECOND));
        return timestampToHourPath(timestamp) + minute + "-" + second + ".jpg";
    }

    private String getProcessName(WinDef.HWND hwnd) {
        User32 user32 = User32.INSTANCE;
        IntByReference processId = new IntByReference();
        user32.GetWindowThreadProcessId(hwnd, processId);
        WinNT.HANDLE processHandle = Kernel32.INSTANCE.OpenProcess(
                Kernel32.PROCESS_QUERY_LIMITED_INFORMATION,
                false,
                processId.getValue());
        char[] processName = new char[4096];
        if (Psapi.INSTANCE.GetModuleFileNameExW(processHandle, null, processName, processName.length) == 0) {
            throw new Win32Exception(Kernel32.INSTANCE.GetLastError());
        }
        Kernel32.INSTANCE.CloseHandle(processHandle);
        return Native.toString(processName);
    }
}
