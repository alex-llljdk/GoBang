
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.media.Manager;
import javax.media.Player;

@SuppressWarnings("restriction")
public class Music {
   public Player audioPlayer = null;//建立一个播放接口
    public Music(URL url) throws Exception{//创建一个准备Player,准备好播放
        audioPlayer = Manager.createRealizedPlayer(url);
    }
    @SuppressWarnings("deprecation")
    public Music(File file) throws MalformedURLException, Exception{//将本地文件改为URL
        this(file.toURL());
    }

    public void play(){//直接调用播放方法就可以
        audioPlayer.start();
    }

    public void stop(){//停止的时候一定要释放资源
        audioPlayer.stop();
        audioPlayer.close();
    }

}

