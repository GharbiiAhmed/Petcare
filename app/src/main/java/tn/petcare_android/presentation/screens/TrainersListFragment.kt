package tn.petcare_android.presentation.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import tn.petcare_android.presentation.viewmodel.TrainersViewModel

class TrainersListFragment : Fragment() {

    private lateinit var viewModel: TrainersViewModel

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
        
        // Load all trainers
        viewModel.getAllTrainers()
        
        // Observe trainers
        viewModel.trainers.observe(viewLifecycleOwner) { trainers ->
            // Update UI with trainers list
            // Implementation depends on your UI framework (Compose or XML layouts)
        }
        
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            // Show error message
        }
        
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            // Show/hide loading indicator
        }
    }
}
