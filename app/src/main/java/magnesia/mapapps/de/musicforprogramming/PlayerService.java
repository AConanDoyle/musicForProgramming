package magnesia.mapapps.de.musicforprogramming;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class PlayerService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    //media player stuff
    private MediaPlayer player;
    private String song;
    private int songPosn;

    private final IBinder musicBind = new MusicBinder();
    private Intent broadcastIntent = new Intent();


    public PlayerService() {
    }

    public void onCreate(){
        super.onCreate();
        songPosn=0;
        player = new MediaPlayer();
        initMusicPlayer();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent){
        player.stop();
        player.release();
        return false;
    }

    public void initMusicPlayer(){
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    private void setNotification() {
        // TODO: Notification einrichten
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.drawable.ic_launcher);
        mBuilder.setContentTitle("Notification Alert!");
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // notificationID allows you to update the notification later on.
        mNotificationManager.notify(001, mBuilder.build());
    }

    private void cancelNotification () {

    }


    public void setSong(String theSong){
        song=theSong;
    }

    public void playSong(){
        player.reset();

        try {
            player.setDataSource(song);
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                }
            });

        } catch (Exception e) {
            Log.e("Start stream", e.getMessage());
        }

        broadcastIntent.setAction("Start ProgressDialog");
        sendBroadcast(broadcastIntent);
        player.prepareAsync();

    }

    public void pauseSong() {
        player.pause();
        songPosn = player.getCurrentPosition();
    }

    public void stopSong() {
        player.stop();
    }

    public void playResume () {
        player.seekTo(songPosn);
        player.start();
    }

    public void fastforwardfSong() {
        player.seekTo(player.getCurrentPosition() + 30000);
    }

    public void rewindSong() {
        player.seekTo(player.getCurrentPosition() - 30000);
    }

    public class MusicBinder extends Binder {
        PlayerService getService() {
            return PlayerService.this;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //start playback
        mp.start();
        broadcastIntent.setAction("Stop ProgressDialog");
        sendBroadcast(broadcastIntent);
    }

}
