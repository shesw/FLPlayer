package com.compassl.anji.flsts.util;

import com.compassl.anji.flsts.db.SongInfo;

import org.litepal.crud.DataSupport;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2017/11/2.
 */
public class TextHandle {


    /**
     * 解析配置文件的行内容，返回：
     * 1.歌名
     * 2.urlMp3
     * 3.urlLyc
     * 4.urlBgs
     */
    public static String[] handleInfo(String line){
        int n_index = line.indexOf("[n!")+3;
        int m_index = line.indexOf("[m!")+3;
        int l_index = line.indexOf("[l!")+3;
        int b_index = line.indexOf("[b!")+3;
        int p_index = line.indexOf("[p!")+3;
        String name = line.substring(n_index,line.indexOf("]",n_index));
        String urlMp3 = line.substring(m_index,line.indexOf("]",m_index));
        String urlLyc = line.substring(l_index,line.indexOf("]",l_index));
        String urlBgs = line.substring(b_index,line.indexOf("]",b_index));
        String urlImg = line.substring(p_index,line.indexOf("]",p_index));
        return new String[]{name,urlMp3,urlLyc,urlBgs,urlImg};
    }


    //在新建文件夹内，根据id内容,从数据库中获得具体文件的下载地址
    public static String[] getWholeFilePath(int id){
        List<SongInfo> infos = DataSupport.select("urlMp3","urlLyc","urlBgs")
                .where("song_id=?",id+"")
                .find(SongInfo.class);
        String urlMp3 = infos.get(0).getUrlMp3();
        String urlLyc = infos.get(0).getUrlLyc();
        String urlBgs = infos.get(0).getUrlBgs();
        return new String[]{urlMp3,urlLyc,urlBgs};
    }

//    //在新建文件夹内，根据id内容获得具体文件的下载地址
//    public static String[] getWholeFilePath(Context context,int id){
//        String line = "";
//        try {
//            InputStream is = new FileInputStream(context.getFilesDir().getAbsolutePath()+"/FLMusic/song_info.txt");
//            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
//            while (id-- >0){
//                reader.readLine();
//            }
//            line = reader.readLine();
//            is.close();
//            reader.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        int m_index = line.indexOf("[m!")+3;
//        int l_index = line.indexOf("[l!")+3;
//        int b_index = line.indexOf("[b!")+3;
//        String urlMp3 = line.substring(m_index,line.indexOf("]",m_index));
//        String urlLyc = line.substring(l_index,line.indexOf("]",l_index));
//        String urlBgs = line.substring(b_index,line.indexOf("]",b_index));
//        return new String[]{urlMp3,urlLyc,urlBgs};
//    }

    public static String getLrcInfo(String allLrc){
        //Matcher m = Pattern.compile("\\[(\\d{1,2}):(\\d{1,2}).(\\d{1,2})\\]").matcher(allLrc);
        if ("".equals(allLrc)){return "";}
        Matcher m = Pattern.compile("\\[(\\d*):(\\d*).(\\d*)\\]").matcher(allLrc);
        int i;
        if (m.find()){
            i= m.start();
        }else {
            return "无歌词信息";
        }
        String str = allLrc.substring(0,i-1);
        return "\r\n\r\n\r\n"+str.replace("[ti:","歌曲：").replace("[ar:","作曲：").replace("[al:","专辑：").replace("[by:","作词：")
                .replaceAll("]","");
    }

}