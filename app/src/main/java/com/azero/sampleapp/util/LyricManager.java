
package com.azero.sampleapp.util;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;

import com.azero.sampleapp.activity.playerinfo.bean.LineBean;
import com.azero.sampleapp.activity.playerinfo.bean.LyricBean;
import com.azero.sdk.util.log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class LyricManager {

    private OnProgressChangedListener progressChangedListener;
    private int selectedColor = Color.parseColor("#1E90FF");
    private int normalColor = Color.parseColor("#FFFFFF");
    private LyricBean lyricBean = null;

    private boolean flag_refresh = false;
    private int flag_position = 0;

    private LyricManager(Context context) {
    }

    public static LyricManager getInstance(Context context) {
        return new LyricManager(context);
    }

    /**
     * 设置歌词流文件
     *
     * @param inputStream 歌词文件的输入流
     */
    public void setFileStream(final InputStream inputStream) {
        log.d("setFileStream***");
        if (null != inputStream) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    decode(inputStream);
                }
            }).start();
        } else {
            lyricBean = null;
        }
    }

    /**
     * 设置歌词文件
     *
     * @param file 歌词文件
     */
    public void setLyricFile(File file) {
        try {
            setFileStream(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据输入流解析转码成歌词
     */
    private void decode(InputStream inputStream) {
        log.d("decode***");
        try {
            String line;
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            BufferedReader bufferedReader = getReader(bufferedInputStream);
            lyricBean = new LyricBean();
            while ((line = bufferedReader.readLine()) != null) {
                analyzeLyric(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        setCurrentTimeMillis(0);
    }

    /**
     * 动态识别歌词编码格式
     *  防止出现乱码问题
     */
    private BufferedReader getReader(BufferedInputStream bufferedInputStream)throws IOException{
        BufferedReader reader = null;
        bufferedInputStream.mark(4);
        byte[] first3bytes = new byte[3];
        bufferedInputStream.read(first3bytes);
        bufferedInputStream.reset();
        if (first3bytes[0] == (byte) 0xEF && first3bytes[1] == (byte) 0xBB
                && first3bytes[2] == (byte) 0xBF) {// utf-8
            log.e("Lyric text type = utf-8");
            reader = new BufferedReader(new InputStreamReader(bufferedInputStream, "UTF-8"));

        } else if (first3bytes[0] == (byte) 0xFF
                && first3bytes[1] == (byte) 0xFE) {
            log.e("Lyric text type = utf-unicode");
            reader = new BufferedReader(
                    new InputStreamReader(bufferedInputStream, "unicode"));
        } else if (first3bytes[0] == (byte) 0xFE
                && first3bytes[1] == (byte) 0xFF) {
            log.e("Lyric text type = utf-16be");
            reader = new BufferedReader(new InputStreamReader(bufferedInputStream,
                    "utf-16be"));
        } else if (first3bytes[0] == (byte) 0xFF
                && first3bytes[1] == (byte) 0xFF) {
            log.e("Lyric text type = utf-16le");
            reader = new BufferedReader(new InputStreamReader(bufferedInputStream,
                    "utf-16le"));
        } else {
            log.e("Lyric text type = GBK");
            reader = new BufferedReader(new InputStreamReader(bufferedInputStream, "UTF-8"));
        }
        return reader;
    }
    /**
     * 逐行解析歌词文件
     */
    private void analyzeLyric(String line) {
        log.e("Lyric line"+line);
        try {
            LineBean lineBean = new LineBean();
            int index = line.lastIndexOf("]");
            if (line != null && line.startsWith("[offset:")) {
                // 时间偏移量
                String string = line.substring(8, index).trim();
                lyricBean.setOffset(Long.parseLong(string));
                return;
            }
            if (line != null && line.startsWith("[ti:")) {
                // title 标题
                String string = line.substring(4, index).trim();
                lyricBean.setTitle(string);
                return;
            }
            if (line != null && line.startsWith("[ar:")) {
                // artist 作者
                String string = line.substring(4, index).trim();
                lyricBean.setArtist(string);
                return;
            }
            if (line != null && line.startsWith("[al:")) {
                // album 所属专辑
                String string = line.substring(4, index).trim();
                lyricBean.setAlbum(string);
                return;
            }
            if (line != null && line.startsWith("[by:")) {
                return;
            }
            if (line != null && index == 9 && line.trim().length() > 10) {
                // 歌词内容
                lineBean.setContent(line.substring(10, line.length()));
                lineBean.setStart(analyzeStartTimeMillis(line.substring(0, 10)));
                lyricBean.getLines().add(lineBean);
            }

            //兼容不同的时间戳格式
            if (line != null && index == 10 && line.trim().length() > 11) {
                // 歌词内容
                lineBean.setContent(line.substring(11, line.length()));
                lineBean.setStart(analyzeStartTimeMillis(line.substring(0, 11)));
                lyricBean.getLines().add(lineBean);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 从字符串中获得时间值
     */
    private long analyzeStartTimeMillis(String str) {
        long minute = Long.parseLong(str.substring(1, 3));
        long second = Long.parseLong(str.substring(4, 6));
        long millisecond = Long.parseLong(str.substring(7, 9));
        return millisecond + second * 1000 + minute * 60 * 1000;
    }

    /**
     * 根据当前时间戳值获得歌词
     *
     * @param timeMillis long值时间戳
     */
    public void setCurrentTimeMillis(final long timeMillis) {
        try {
            final List<LineBean> lines = lyricBean != null ? lyricBean.getLines() : null;
            if (lines != null) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            int position = 0;
                            SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
                            for (int i = 0, size = lines.size(); i < size; i++) {
                                if (lines.get(i).getStart() < timeMillis) {
                                    position = i;
                                } else {
                                    break;
                                }
                            }
                            if (position == flag_position && !flag_refresh) {
                                return;
                            }
                            flag_position = position;
                            for (int i = 0, size = lines.size(); i < size; i++) {
                                if (i != position) {
                                    ForegroundColorSpan span = new ForegroundColorSpan(normalColor);
                                    String line = lines.get(i).getContent();
                                    SpannableString spannableString = new SpannableString(line + "\n");
                                    spannableString.setSpan(span, 0, spannableString.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                                    stringBuilder.append(spannableString);
                                } else {
                                    ForegroundColorSpan span = new ForegroundColorSpan(selectedColor);
                                    String line = lines.get(i).getContent();
                                    SpannableString spannableString = new SpannableString(line + "\n");
                                    spannableString.setSpan(span, 0, spannableString.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                                    stringBuilder.append(spannableString);
                                }
                            }
                            Message message = new Message();
                            message.what = 0x159;
                            DataHolder dataHolder = new DataHolder();
                            dataHolder.builder = stringBuilder;
                            dataHolder.position = position;
                            dataHolder.refresh = flag_refresh;
                            dataHolder.lines = lines;
                            message.obj = dataHolder;
                            handler.sendMessage(message);

                            if (flag_refresh) {
                                flag_refresh = false;
                            }
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                }).start();
            } else {
                Message message = new Message();
                message.what = 0x159;
                DataHolder dataHolder = new DataHolder();
                dataHolder.builder = null;
                dataHolder.position = -1;
                message.obj = dataHolder;
                handler.sendMessage(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Object object = msg.obj;
            if (object != null && object instanceof DataHolder) {
                DataHolder dataHolder = (DataHolder) object;
                switch (msg.what) {
                    case 0x159:
                        if (null != progressChangedListener) {
                            progressChangedListener.onProgressChanged(dataHolder.builder, dataHolder.position, dataHolder.refresh);
                            if (dataHolder.lines != null&&dataHolder.position<dataHolder.lines.size()) {
                                progressChangedListener.onProgressChanged(dataHolder.lines.get(dataHolder.position).getContent(), dataHolder.refresh);
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    };

    /**
     * 数据缓存类
     */
    class DataHolder {
        SpannableStringBuilder builder;
        List<LineBean> lines;
        boolean refresh;
        int position;
    }

    /**
     * 注册监听事件
     */
    public void setOnProgressChangedListener(OnProgressChangedListener progressChangedListener) {
        this.progressChangedListener = progressChangedListener;
    }

    /**
     *
     * */
    public interface OnProgressChangedListener {
        public void onProgressChanged(String singleLine, boolean refresh);

        public void onProgressChanged(SpannableStringBuilder stringBuilder, int lineNumber, boolean refresh);
    }

    /**
     * 设置选中文本颜色
     */
    public void setSelectedTextColor(int color) {
        if (color != selectedColor) {
            selectedColor = color;
            flag_refresh = true;
        }
    }

    /**
     * 设置正常文本颜色
     */
    public void setNormalTextColor(int color) {
        if (color != normalColor) {
            normalColor = color;
        }
    }
}
