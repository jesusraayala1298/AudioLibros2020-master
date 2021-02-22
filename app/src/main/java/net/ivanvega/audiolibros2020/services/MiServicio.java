package net.ivanvega.audiolibros2020.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import net.ivanvega.audiolibros2020.MainActivity;
import net.ivanvega.audiolibros2020.R;

import java.io.IOException;
import java.util.Random;

//AL CREAR UN SERVICIO ES NECESARIO CREAR LA SIGUEINTE CLASE QUE EXTIENDE DE SERVICE E IMPLEMENTEAR SUS METODOS
//CORRESPONDIENTES, ADEMAS DE ESO PARA  ESTA APLICACION ES IMPORTANTE IMPLEMENTEAR EL MediaPlayerControl Y
//EL metodo OnPreparedListener ESTO PARA PROGRAMAR EL FUNCIONAMIENTO DEL REPRODUCTOR DE AUDIO.
public class MiServicio extends Service implements MediaController.MediaPlayerControl, MediaPlayer.OnPreparedListener {
    //OBJETOS PARA EL MANEJO DEL MEDIA PLAYER
    public static MediaPlayer mediaPlayer;
    private final IBinder binder = new MiServicioBinder();
    private final Random mGenerator = new Random();
    //EL METODO onStop ES USADO PARA DETENER EL MEDIA PLAYER Y DETENER EL SERVICIO CUANDO TERMINE O SE PAUSE EL
    //AUDIO
    public void onStop() {
        try {
            mediaPlayer.stop();
            mediaPlayer.reset();
            stopSelf();
            stopForeground(true);
        } catch (Exception e) {
            Log.d("Audiolibros", "Error en mediaPlayer.stop()");
        }
    }
    //METODO start INICIA EL MEDIA PLAYER Y EL SERVICIO
    @Override
    public void start() {
        mediaPlayer.start();
        if (mediaPlayer.isPlaying()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel();
                foregroundService();
            }
        }
    }
    //METODO pause ES USADO PARA DETENER MOMENTANEAMENTE EL AUDIO Y EL SERVICIO
    @Override
    public void pause() {
        mediaPlayer.pause();
        stopForeground(true);
    }
    //METODO PARA OBTENER LA DURACION DEL AUDIO
    @Override
    public int getDuration() {
        try {
            return mediaPlayer.getDuration();
        }catch (Exception e){
            return 0;
        }
    }
    //METODO PARA OBTENER LA POSICION EN LA CUAL SE LE DIO PAUSA AL AUDIO
    @Override
    public int getCurrentPosition() {
        try{
    return mediaPlayer.getCurrentPosition();
        }catch(Exception e){
    return 0;
        }
    }
    @Override
    public void seekTo(int i) {
        mediaPlayer.seekTo(i);
    }
    //METODO PARA VERIFICAR SI EL AUDIO ESTA ACTIVO
    @Override
    public boolean isPlaying() {
try {
    return mediaPlayer.isPlaying();
}catch(Exception e){
    return false;
}
    }
    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return mediaPlayer.getAudioSessionId();
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class MiServicioBinder extends Binder {
        public MiServicio getService() {
            // Return this instance of LocalService so clients can call public methods
            return MiServicio.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    /** method for clients */
    public int getRandomNumber() {
        return mGenerator.nextInt(100);
    }
    //METODO INICIAL CUANDO SE LANZA EL SERVICIO
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("MSAL", "servicio creado");
    }

    //EL SIGUIENTE METODO ES PARA CREAR UNA CANAL PARA QUE SE MUESTRE LA NOTIFICACION EN PRIMER PLANO DEL SERVICIO
    private String CHANNEL_ID="CANALID";
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    //EL METODO onStartCommand ES EL METODO QUE INDICA LAS FUNCIONES QUE VA A REALIZAR EL SERVICIO, EN ESTE CASO INICIAR
    //EL AUDIO A TRAVES DE UNA TAREA ASINCRONA
    public int id;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String url = intent.getStringExtra("URL");
        id = intent.getIntExtra("ID",0);
        Log.d("MSAL", "Iniciando la tarea pesada ");
                AsyncTask<String,
                        String, Boolean> task = new AsyncTask<String, String, Boolean>() {
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
mediaPlayer = new MediaPlayer();
                    //Inicializacion de objetos
                }
                //ESTAS SON LAS OPERACIONES QUE VA A REALIZAR EL SERVICIO MIENTRAS ESTE CORRIENDO
                @RequiresApi(api = Build.VERSION_CODES.O)
                @SuppressLint("WrongThread")
                @Override
                protected Boolean doInBackground(String... urls) {
                    Uri audio = Uri.parse(urls[0]);
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        Log.e("holi", "que pedo"+audio);
                        mediaPlayer=MediaPlayer.create(getApplicationContext(),Uri.parse(urls[0]));
                    mediaPlayer.setOnPreparedListener(v->{
                        mediaPlayer.start();
                    });
                    mediaPlayer.setOnCompletionListener(v->{
                        onStop();
                    });
                        createNotificationChannel();
                        foregroundService();
                    return urls.length > 0;
                }

                @Override
                protected void onProgressUpdate(String... values) {
                    super.onProgressUpdate(values);
                    Log.d("MSAL", "Iniciando la tarea pesada " + values[0]);

                }

                @Override
                protected void onPostExecute(Boolean aBoolean) {
                    super.onPostExecute(aBoolean);
                    if(aBoolean){
                        Log.d("MSAL", "Tarea exhautiva finalizada");
                    }
                }
            };
                //EJECUTAR LA TAREA ASINCRONA CON EL URL DEL AUDIO
            task.execute(url);
        return super.onStartCommand(intent, flags, startId);
    }

    //ESTE METODO REPRESENTA LA INFORMACION QUE SE VA A MOSTRAR EN LA NOTIFICACION, INICIA EL SERVICIO DE PRIMER PLANO
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void foregroundService() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.putExtra("rep", "Servicio Primer plano");
        notificationIntent.putExtra("ID", id);

        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 1002, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification =
                new Notification.Builder(this, CHANNEL_ID)
                        .setContentTitle("Titulo")
                        .setContentText("Servicio en ejecucion")
                        .setSmallIcon(R.drawable.ic_launcher_background)
                        .setContentIntent(pendingIntent)
                        .setTicker("Se inicio el servicio")
                        .build();
             startForeground(1000, notification);

    }
    //METODO QUE OCURRE CUANDO SE DESTRUYE O FINALIZA EL SERVICIO
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("MSAL", "Servicio destruido");
    }
}
