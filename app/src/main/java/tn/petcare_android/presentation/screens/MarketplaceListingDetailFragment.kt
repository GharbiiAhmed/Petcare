package tn.petcare_android.presentation.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import tn.petcare_android.presentation.viewmodel.MarketplaceViewModel

class MarketplaceListingDetailFragment : Fragment() {

    private lateinit var viewModel: MarketplaceViewModel
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
        
        viewModel = ViewModelProvider(this).get(MarketplaceViewModel::class.java)
        
        // Get listing ID from arguments
        listingId = arguments?.getString("listingId")
        
        listingId?.let {
            // Load listing details
            viewModel.getListing(it)
            
            // Observe listing
            viewModel.selectedListing.observe(viewLifecycleOwner) { listing ->
                // Update UI with full listing details
                // Display: all pet info, price, seller contact, images
                // Show "Send Inquiry" button
                // Show like button
            }
        }
    }

    companion object {
        fun newInstance(listingId: String) = MarketplaceListingDetailFragment().apply {
            arguments = Bundle().apply {
                putString("listingId", listingId)
            }
        }
    }
}
