package com.stringsattached.qrcodescanner.ui.home

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.stringsattached.qrcodescanner.databinding.FragmentHomeBinding
import kotlin.math.max

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val requestCodeCameraPermission = 1001
    private lateinit var codeScanner: CodeScanner

    private lateinit var viewModel: HomeViewModel

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (ContextCompat.checkSelfPermission(
                requireContext(), android.Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            askForCameraPermission()
        } else {
            setupZoomButtons()
            setupCameraRotationButton()
            setupBatchButton()
            setupQRCodeScanner()
        }
    }

    private fun setupBatchButton() {
        binding.batchAction.setOnClickListener {
            if (codeScanner.scanMode == ScanMode.SINGLE) {
                codeScanner.scanMode = ScanMode.CONTINUOUS
            } else {
                codeScanner.scanMode = ScanMode.SINGLE
            }
        }
    }

    private fun setupCameraRotationButton() {
        binding.cameraRotationAction.setOnClickListener {
            if (codeScanner.camera == CodeScanner.CAMERA_BACK) {
                codeScanner.camera = CodeScanner.CAMERA_FRONT
            } else {
                codeScanner.camera = CodeScanner.CAMERA_BACK
            }
        }
    }

    private fun setupZoomButtons() {
        binding.zoomControls.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                codeScanner.zoom = binding.zoomControls.progress
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                // no - op
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                // no - op
            }
        })
        binding.zoomAdd.setOnClickListener {
            binding.zoomControls.progress = (binding.zoomControls.progress / 10) * 10 + 10
            codeScanner.zoom = binding.zoomControls.progress
        }

        binding.zoomRemove.setOnClickListener {
            binding.zoomControls.progress = max(0, (binding.zoomControls.progress / 10) * 10 - 10)
            codeScanner.zoom = binding.zoomControls.progress
        }
    }


    private fun setupQRCodeScanner() {
        val scannerView = binding.scannerView

        codeScanner = CodeScanner(requireContext(), scannerView)

        codeScanner.camera = CodeScanner.CAMERA_FRONT // or CAMERA_FRONT or specific camera id
        codeScanner.formats = CodeScanner.ALL_FORMATS // list of type BarcodeFormat,
        // ex. listOf(BarcodeFormat.QR_CODE)
        codeScanner.autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
        codeScanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
        codeScanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
        codeScanner.isFlashEnabled = false // Whether to enable flash or not

        // Callbacks
        codeScanner.decodeCallback = DecodeCallback {
            activity?.runOnUiThread {
                Toast.makeText(requireContext(), "Scan result: ${it.text}", Toast.LENGTH_LONG)
                    .show()
            }
        }
        codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
            activity?.runOnUiThread {
                Toast.makeText(
                    requireContext(), "Camera initialization error: ${it.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
    }

    private fun askForCameraPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(android.Manifest.permission.CAMERA),
            requestCodeCameraPermission
        )
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}