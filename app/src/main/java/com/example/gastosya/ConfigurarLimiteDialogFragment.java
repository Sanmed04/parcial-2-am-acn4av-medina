package com.example.gastosya;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import android.widget.Toast;

public class ConfigurarLimiteDialogFragment extends DialogFragment {

    public ConfigurarLimiteDialogFragment() {
        setStyle(STYLE_NORMAL, R.style.DialogTheme);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_configurar_limite, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextInputEditText setLimiteGasto = view.findViewById(R.id.setLimiteGasto);
        MaterialButton btnGuardarLimite = view.findViewById(R.id.btnGuardarLimite);

        if (setLimiteGasto == null || btnGuardarLimite == null) {
            Toast.makeText(getActivity(), "Error al cargar el diálogo", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }

        if (getActivity() instanceof MainActivity) {
            setLimiteGasto.setText(String.valueOf(((MainActivity) getActivity()).getLimiteGastos()));
        }

        btnGuardarLimite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String limiteStr = setLimiteGasto.getText().toString();
                if (!limiteStr.isEmpty()) {
                    try {
                        double nuevoLimite = Double.parseDouble(limiteStr);
                        if (nuevoLimite <= 0) {
                            Toast.makeText(getActivity(), "El límite debe ser mayor a 0", Toast.LENGTH_SHORT).show();
                        } else if (getActivity() instanceof MainActivity) {
                            ((MainActivity) getActivity()).setLimiteGastos(nuevoLimite);
                            ((MainActivity) getActivity()).guardarLimiteEnFirestore(nuevoLimite);
                            dismiss();
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(getActivity(), "Por favor ingrese un valor válido", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Ingrese un límite", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
            );
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }
}