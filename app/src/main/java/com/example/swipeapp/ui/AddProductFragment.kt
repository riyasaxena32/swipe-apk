package com.example.swipeapp.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.example.swipeapp.R
import com.example.swipeapp.api.AddProductResponse
import com.example.swipeapp.api.ProductService
import com.example.swipeapp.data.AppDatabase
import com.example.swipeapp.data.PendingProduct
import com.example.swipeapp.data.SyncStatus
import com.example.swipeapp.network.NetworkModule
import com.example.swipeapp.work.ProductSyncWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class AddProductFragment : BottomSheetDialogFragment() {
    private lateinit var productNameLayout: TextInputLayout
    private lateinit var productNameInput: TextInputEditText
    private lateinit var productTypeLayout: TextInputLayout
    private lateinit var productTypeDropdown: AutoCompleteTextView
    private lateinit var priceLayout: TextInputLayout
    private lateinit var priceInput: TextInputEditText
    private lateinit var taxLayout: TextInputLayout
    private lateinit var taxInput: TextInputEditText
    private lateinit var productImage: ImageView
    private lateinit var submitButton: MaterialButton
    private lateinit var progressBar: ProgressBar

    private var selectedImageUri: Uri? = null
    private val productTypes = listOf("Product", "Service")
    private var productUpdateListener: ProductUpdateListener? = null
    private lateinit var database: AppDatabase

    private val productService = NetworkModule.productService

    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                productImage.scaleType = ImageView.ScaleType.CENTER_CROP
                productImage.setImageURI(uri)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        database = AppDatabase.getDatabase(context)
        // Try to find the listener from parent fragment first
        productUpdateListener = parentFragment as? ProductUpdateListener
        // If not found in parent fragment, try activity
        if (productUpdateListener == null) {
            productUpdateListener = context as? ProductUpdateListener
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_product, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViews(view)
        setupProductTypeDropdown()
        setupImagePicker()
        setupSubmitButton()
    }

    private fun initializeViews(view: View) {
        productNameLayout = view.findViewById(R.id.productNameLayout)
        productNameInput = view.findViewById(R.id.productNameInput)
        productTypeLayout = view.findViewById(R.id.productTypeLayout)
        productTypeDropdown = view.findViewById(R.id.productTypeDropdown)
        priceLayout = view.findViewById(R.id.priceLayout)
        priceInput = view.findViewById(R.id.priceInput)
        taxLayout = view.findViewById(R.id.taxLayout)
        taxInput = view.findViewById(R.id.taxInput)
        productImage = view.findViewById(R.id.productImage)
        submitButton = view.findViewById(R.id.submitButton)
        progressBar = view.findViewById(R.id.progressBar)
    }

    private fun setupProductTypeDropdown() {
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, productTypes)
        productTypeDropdown.setAdapter(adapter)
    }

    private fun setupImagePicker() {
        productImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            getContent.launch(intent)
        }
    }

    private fun setupSubmitButton() {
        submitButton.setOnClickListener {
            if (validateInputs()) {
                uploadProduct()
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        if (productNameInput.text.isNullOrBlank()) {
            productNameLayout.error = "Product name is required"
            isValid = false
        } else {
            productNameLayout.error = null
        }

        if (productTypeDropdown.text.isNullOrBlank()) {
            productTypeLayout.error = "Product type is required"
            isValid = false
        } else {
            productTypeLayout.error = null
        }

        if (priceInput.text.isNullOrBlank()) {
            priceLayout.error = "Price is required"
            isValid = false
        } else {
            try {
                priceInput.text.toString().toDouble()
                priceLayout.error = null
            } catch (e: NumberFormatException) {
                priceLayout.error = "Invalid price format"
                isValid = false
            }
        }

        if (taxInput.text.isNullOrBlank()) {
            taxLayout.error = "Tax rate is required"
            isValid = false
        } else {
            try {
                taxInput.text.toString().toDouble()
                taxLayout.error = null
            } catch (e: NumberFormatException) {
                taxLayout.error = "Invalid tax rate format"
                isValid = false
            }
        }

        return isValid
    }

    private fun uploadProduct() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                progressBar.visibility = View.VISIBLE
                submitButton.isEnabled = false

                // Save image to internal storage if selected
                val savedImagePath = selectedImageUri?.let { uri ->
                    saveImageToInternalStorage(uri)
                }

                if (isNetworkAvailable()) {
                    // Online - Upload directly
                    uploadProductToServer(savedImagePath)
                } else {
                    // Offline - Save locally
                    saveProductLocally(savedImagePath)
                }
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                progressBar.visibility = View.GONE
                submitButton.isEnabled = true
            }
        }
    }

    private suspend fun uploadProductToServer(savedImagePath: String?) {
        val productName = productNameInput.text.toString()
            .toRequestBody("text/plain".toMediaTypeOrNull())
        val productType = productTypeDropdown.text.toString()
            .toRequestBody("text/plain".toMediaTypeOrNull())
        val price = priceInput.text.toString()
            .toRequestBody("text/plain".toMediaTypeOrNull())
        val tax = taxInput.text.toString()
            .toRequestBody("text/plain".toMediaTypeOrNull())

        val imagePart = savedImagePath?.let { path ->
            val file = File(path)
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("files[]", file.name, requestFile)
        }

        val response = withContext(Dispatchers.IO) {
            productService.addProduct(productName, productType, price, tax, imagePart)
        }

        if (response.isSuccessful && response.body()?.success == true) {
            showSuccessDialog(response.body()!!)
        } else {
            Toast.makeText(
                context,
                "Failed to add product: ${response.message()}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private suspend fun saveProductLocally(savedImagePath: String?) {
        val pendingProduct = PendingProduct(
            productName = productNameInput.text.toString(),
            productType = productTypeDropdown.text.toString(),
            price = priceInput.text.toString().toDouble(),
            tax = taxInput.text.toString().toDouble(),
            imagePath = savedImagePath
        )

        withContext(Dispatchers.IO) {
            database.pendingProductDao().insert(pendingProduct)
        }

        showOfflineSuccessDialog()
        scheduleSync()
    }

    private fun saveImageToInternalStorage(uri: Uri): String {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val file = File(requireContext().filesDir, "product_images/${System.currentTimeMillis()}.jpg")
        file.parentFile?.mkdirs()
        
        FileOutputStream(file).use { outputStream ->
            inputStream?.copyTo(outputStream)
        }
        return file.absolutePath
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities != null &&
                (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                 capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
    }

    private fun showOfflineSuccessDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Saved Offline")
            .setMessage("Product saved locally and will be uploaded when internet connection is available.")
            .setPositiveButton("OK") { _, _ ->
                productUpdateListener?.onProductAdded()
                dismiss()
            }
            .show()
    }

    private fun scheduleSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWork = OneTimeWorkRequestBuilder<ProductSyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(requireContext())
            .enqueueUniqueWork(
                "product_sync",
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                syncWork
            )
    }

    private fun showSuccessDialog(response: AddProductResponse) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Success")
            .setMessage("Product added successfully!\nProduct ID: ${response.product_id}")
            .setPositiveButton("OK") { _, _ ->
                productUpdateListener?.onProductAdded()
                dismiss()
            }
            .show()
    }

    companion object {
        fun newInstance() = AddProductFragment()
    }
} 