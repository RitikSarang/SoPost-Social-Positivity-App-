package com.example.sopost


import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.media.ThumbnailUtils
import android.opengl.Visibility
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.sopost.ml.Model
import com.example.sopost.viewmodel.PostViewModel
import kotlinx.android.synthetic.main.fragment_post_check.*
import kotlinx.android.synthetic.main.fragment_post_check.imageView
import kotlinx.android.synthetic.main.fragment_post_success.*
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder


class PostCheckFragment : Fragment(R.layout.fragment_post_check) {
    lateinit var viewModel: PostViewModel
    var imageSize = 224
    private val IMAGE_PICK_CODE = 1000
    private val PERMISSION_CODE = 1001

    private val args by navArgs<PostCheckFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        ).get(PostViewModel::class.java)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageUri = args.postCheck.img_URI

        var image = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, imageUri)
        val dimension = Math.min(image.width, image.height)
        image = ThumbnailUtils.extractThumbnail(image, dimension, dimension)
        imageView.setImageBitmap(image)
        image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false)
        classifyImage(image)

        img_proceed.setOnClickListener {
            val user = args.postCheck
            viewModel.insertPost(user.img_URI, user.title, user.desc, user.count)
            findNavController().popBackStack(R.id.navhomeFragment,true)
        }
    }


    fun classifyImage(image: Bitmap) {

        try {
            val model: Model = Model.newInstance(requireContext())

            // Creates inputs for reference.
            val inputFeature0 =
                TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)
            val byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3)
            byteBuffer.order(ByteOrder.nativeOrder())

            // get 1D array of 224 * 224 pixels in image
            val intValues = IntArray(imageSize * imageSize)
            image.getPixels(intValues, 0, image.width, 0, 0, image.width, image.height)

            // iterate over pixels and extract R, G, and B values. Add to bytebuffer.
            var pixel = 0
            for (i in 0 until imageSize) {
                for (j in 0 until imageSize) {
                    val `val` = intValues[pixel++] // RGB
                    byteBuffer.putFloat((`val` shr 16 and 0xFF) * (1f / 255f))
                    byteBuffer.putFloat((`val` shr 8 and 0xFF) * (1f / 255f))
                    byteBuffer.putFloat((`val` and 0xFF) * (1f / 255f))
                }
            }
            inputFeature0.loadBuffer(byteBuffer)

            // Runs model inference and gets result.
            val outputs: Model.Outputs = model.process(inputFeature0)
            val outputFeature0: TensorBuffer = outputs.getOutputFeature0AsTensorBuffer()
            val confidences = outputFeature0.floatArray
            // find the index of the class with the biggest confidence.
            var maxPos = 0
            var maxConfidence = 0f
            for (i in confidences.indices) {
                if (confidences[i] > maxConfidence) {
                    maxConfidence = confidences[i]
                    maxPos = i
                }
            }
            val classes = arrayOf("harmful", "notharmful")
            result.text = classes[maxPos]
            var s = ""
            for (i in classes.indices) {
                s += String.format("%s: %.1f%%\n", classes[i], confidences[i] * 100)
            }
            confidence.text = s



            if (classes[maxPos] == "harmful") {

                result.text = "Harmful"
                result.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))

                img_proceed.isVisible = false
                txtNext.isVisible = false
            } else {
                result.text = "Not Harmful"
                result.setTextColor(ContextCompat.getColor(requireContext(), R.color.teal_700))
                img_proceed.isVisible = true
                txtNext.isVisible = true
            }


            // Releases model resources if no longer used.
            model.close()
        } catch (e: IOException) {
            // TODO Handle the exception
        }
    }


}