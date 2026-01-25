package tn.petcare_android.presentation.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import tn.petcare_android.presentation.viewmodel.TrainersViewModel

class TrainerDetailFragment : Fragment() {

    private lateinit var viewModel: TrainersViewModel
    private var trainerId: String? = null

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
        
        viewModel = ViewModelProvider(this).get(TrainersViewModel::class.java)
        
        // Get trainer ID from arguments
        trainerId = arguments?.getString("trainerId")
        
        trainerId?.let {
            // Load trainer details
            viewModel.getTrainer(it)
            
            // Observe trainer
            viewModel.selectedTrainer.observe(viewLifecycleOwner) { trainer ->
                // Update UI with trainer details
                // Display: specialization, hourlyRate, yearsOfExperience, bio, certifications, etc.
                // Show "Book Trainer" button
            }
        }
    }

    companion object {
        fun newInstance(trainerId: String) = TrainerDetailFragment().apply {
            arguments = Bundle().apply {
                putString("trainerId", trainerId)
            }
        }
    }
}
