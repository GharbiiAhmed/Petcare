package tn.petcare_android.presentation.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import tn.petcare_android.presentation.viewmodel.AdoptionViewModel

class AdoptionListFragment : Fragment() {

    private lateinit var viewModel: AdoptionViewModel

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
        
        // Load all available pets for adoption
        viewModel.getAllListings("available")
        
        // Observe listings
        viewModel.listings.observe(viewLifecycleOwner) { listings ->
            // Update UI with listings
            // Display: petName, species, breed, age, images, rescuer info
            // Show like button and application button for each listing
        }
        
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            // Show error message
        }
        
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            // Show/hide loading indicator
        }
    }
}
