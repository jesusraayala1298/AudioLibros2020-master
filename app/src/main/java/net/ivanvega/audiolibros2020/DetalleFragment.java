package net.ivanvega.audiolibros2020;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;

import net.ivanvega.audiolibros2020.services.MiIntentService;
import net.ivanvega.audiolibros2020.services.MiServicio;

import java.io.IOException;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DetalleFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetalleFragment extends Fragment
                            implements View.OnTouchListener {

    public static String ARG_ID_LIBRO = "id_libro";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    //OBJETOS QUE DEFINEN EL SERVICIO, EL MEDIACONTROLLER Y UN INTENT PARA EL MANEJO DEL SERVICIO
    static MiServicio  miServicio;
    static MediaController mediaController;
    Intent iSer;

    public DetalleFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DetalleFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DetalleFragment newInstance(String param1, String param2) {
        DetalleFragment fragment = new DetalleFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mediaController = new MediaController(getActivity());
        miServicio= new MiServicio();
       View vista = inflater.inflate(R.layout.fragment_detalle, container, false);

        Bundle args = getArguments();

        if (args != null) {
            int position = args.getInt(ARG_ID_LIBRO);
            ponInfoLibro(position, vista);
        } else {
            ponInfoLibro(0, vista);
        }



        return vista;
    }

    public void ponInfoLibro(int id) {
        ponInfoLibro(id, getView());
    }

    //METODO QUE REALIZA LA CONEXION CON EL SERVICIO
    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MiServicio.MiServicioBinder miServicioBinder =
                    (MiServicio.MiServicioBinder) iBinder ;
             miServicio =
                    miServicioBinder.getService();
            control();
            Log.d("MSE", "GFrameno enlazado al seervicio " + componentName);

             int randf =  miServicio.getRandomNumber();
            Log.d("MSE", "Peticion  al servicio " + randf);

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    private void ponInfoLibro(int id, View vista) {
        //OBJETO QUE HACE REFERENCIA A NUESTRO SERVICIO
        iSer = new Intent(getContext(), MiServicio.class);
        Libro libro =
                Libro.ejemploLibros().elementAt(id);
        ((TextView) vista.findViewById(R.id.titulo)).setText(libro.titulo);
        ((TextView) vista.findViewById(R.id.autor)).setText(libro.autor);
        ((ImageView) vista.findViewById(R.id.portada)).setImageResource(libro.recursoImagen);

        //IMPLEMENTACION DEL SERVICIO, ALGUNAS VALIDACIONES Y USO DEL MEDIACONTROLLER PARA EL MANEJO VISUAL DEL AUDIO
        //
        try {
            if (id == miServicio.id) {
                if (!miServicio.isPlaying()) {
                    iSer.putExtra("URL", libro.urlAudio);
                    iSer.putExtra("ID", id);
                    getActivity().startService(iSer);
                    getActivity().bindService(iSer, serviceConnection, Context.BIND_AUTO_CREATE);
                } else {
                    mediaController.show(50000);
                }
            } else {
                miServicio.onStop();
                iSer.putExtra("URL", libro.urlAudio);
                iSer.putExtra("ID", id);
                getActivity().startService(iSer);
                getActivity().bindService(iSer, serviceConnection, Context.BIND_AUTO_CREATE);
            }
        }catch(Exception e){
            iSer.putExtra("URL", libro.urlAudio);
            iSer.putExtra("ID",id);
            getActivity().startService(iSer);
            getActivity().bindService(iSer, serviceConnection, Context.BIND_AUTO_CREATE);
        }

        vista.setOnTouchListener(this);
    }
    //METODO PARA QUE EL MEDIACONTROLLER APARESCA CUANDO SE DE CLIC EN LA PANTALLA DEL MOVIL
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        mediaController.show();
        return false;
    }
    //METODO PARA CONTROLAR LA PAUSA DEL AUDIO
    @Override
    public void onResume() {
        super.onResume();
        if(miServicio.isPlaying()){
           mediaController.show(50000);
        }
    }
    //METODO PARA INICIALIZAR EL MEDIACONTROLLER  VINCULARLO CON EL MEDIAPLAYER DEL SERVICIO Y PROPORCIONARLE
    //ALGUNAS CARACTERISTICAS VISUALES.
    public void control() {
        mediaController.setMediaPlayer(miServicio);
        mediaController.setAnchorView(getView());
        mediaController.setEnabled(true);
        mediaController.show(50000);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}