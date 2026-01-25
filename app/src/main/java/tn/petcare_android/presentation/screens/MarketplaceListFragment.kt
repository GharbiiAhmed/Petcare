package tn.petcare_android.presentation.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import tn.petcare_android.presentation.viewmodel.MarketplaceViewModel

class MarketplaceListFragment : Fragment() {

    private lateinit var viewModel: MarketplaceViewModel

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
        
        viewModel = ViewModelProvider(this).get(MarketplaceViewModel::class.java)
        
        // Load all active listings
        viewModel.getAllListings("active")
        
        // Observe listings
        viewModel.listings.observe(viewLifecycleOwner) { listings ->
            // Update UI with listings
            // Display: petName, species, breed, price, images, seller info
            // Show like button and inquiry button for each listing
        }
        
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            // Show error message
        }
        
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            // Show/hide loading indicator
        }
    }
}
