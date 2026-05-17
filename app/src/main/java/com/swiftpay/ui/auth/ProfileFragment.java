package com.swiftpay.ui.auth;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.swiftpay.MainActivity;
import com.swiftpay.R;
import com.swiftpay.data.preferences.SessionManager;
import com.swiftpay.util.ImageUtils;
import com.swiftpay.viewmodel.UserViewModel;
import java.io.File;

/**
 * Fragment de Perfil de usuario.
 * RF 1.10: Ver perfil (nombre, username, rol, foto).
 * RF 1.11: Subir/actualizar foto de perfil.
 */
public class ProfileFragment extends Fragment {

    private UserViewModel viewModel;
    private ImageView ivAvatar;
    private TextView tvName, tvUsername, tvRole;
    private FloatingActionButton fabPhoto;
    private MaterialButton btnChangePassword;
    private SessionManager sessionManager;

    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    handleImageSelected(uri);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(UserViewModel.class);
        sessionManager = ((MainActivity) requireActivity()).getSessionManager();

        ivAvatar = view.findViewById(R.id.iv_profile_avatar);
        tvName = view.findViewById(R.id.tv_profile_name);
        tvUsername = view.findViewById(R.id.tv_profile_username);
        tvRole = view.findViewById(R.id.tv_profile_role);
        fabPhoto = view.findViewById(R.id.fab_change_photo);
        btnChangePassword = view.findViewById(R.id.btn_change_password);

        // Cargar datos del usuario
        long userId = sessionManager.getUserId();
        viewModel.getUserById(userId).observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                tvName.setText(user.getFullName());
                tvUsername.setText(user.getUsername());
                tvRole.setText(user.getRole());

                // Cargar foto de perfil
                if (user.getProfileImagePath() != null) {
                    File imageFile = ImageUtils.getImageFile(requireContext(), user.getProfileImagePath());
                    if (imageFile != null) {
                        ivAvatar.setImageURI(Uri.fromFile(imageFile));
                        ivAvatar.setImageTintList(null);
                    }
                }
            }
        });

        // Seleccionar foto
        fabPhoto.setOnClickListener(v -> pickImage.launch("image/*"));

        // Cambiar contraseña
        btnChangePassword.setOnClickListener(v -> {
            ((MainActivity) requireActivity()).getNavController()
                    .navigate(R.id.changePasswordFragment);
        });

        // Observar operaciones
        viewModel.getOperationMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
        });
    }

    private void handleImageSelected(Uri uri) {
        Bitmap bitmap = ImageUtils.loadBitmapFromUri(requireContext(), uri);
        if (bitmap == null) {
            Toast.makeText(requireContext(), R.string.error_image_too_large, Toast.LENGTH_LONG).show();
            return;
        }
        long userId = sessionManager.getUserId();
        viewModel.uploadProfilePhoto(userId, bitmap);
    }
}
