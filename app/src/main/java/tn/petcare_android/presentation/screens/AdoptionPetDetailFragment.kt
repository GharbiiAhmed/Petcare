package tn.petcare_android.presentation.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import tn.petcare_android.presentation.viewmodel.AdoptionViewModel

class AdoptionPetDetailFragment : Fragment() {

    private lateinit var viewModel: AdoptionViewModel
    private var listingId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            android.R.layout.list_content,
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = ViewModelProvider(this).get(AdoptionViewModel::class.java)
        
        // Get listing ID from arguments
        listingId = arguments?.getString("listingId")
        
        listingId?.let {
            // Load pet details
            viewModel.getListing(it)
            
            // Observe listing
            viewModel.selectedListing.observe(viewLifecycleOwner) { listing ->
                // Update UI with full pet details
                // Display: all pet info, adoption requirements, rescuer contact, images
                // Show "Submit Application" button
                // Show like button
            }
        }
    }

    companion object {
        fun newInstance(listingId: String) = AdoptionPetDetailFragment().apply {
            arguments = Bundle().apply {
                putString("listingId", listingId)
            }
        }
    }
}
