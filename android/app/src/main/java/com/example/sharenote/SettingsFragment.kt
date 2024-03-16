import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.sharenote.MainActivity
import com.example.sharenote.MyPageActivity
import com.example.sharenote.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // Initialize SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE)

        // Set initial switch states based on saved preferences
        binding.switch1.isChecked = sharedPreferences.getBoolean("switch1", false)
        binding.switch2.isChecked = sharedPreferences.getBoolean("switch2", false)
        binding.switch3.isChecked = sharedPreferences.getBoolean("switch3", false)
        binding.switch4.isChecked = sharedPreferences.getBoolean("switch4", false)
        binding.switch5.isChecked = sharedPreferences.getBoolean("switch5", false)

        // Switches
        binding.switch1.setOnCheckedChangeListener { _, isChecked ->
            // Save switch state to SharedPreferences
            sharedPreferences.edit().putBoolean("switch1", isChecked).apply()
        }

        binding.switch2.setOnCheckedChangeListener { _, isChecked ->
            // Save switch state to SharedPreferences
            sharedPreferences.edit().putBoolean("switch2", isChecked).apply()
        }

        binding.switch3.setOnCheckedChangeListener { _, isChecked ->
            // Save switch state to SharedPreferences
            sharedPreferences.edit().putBoolean("switch3", isChecked).apply()
        }

        binding.switch4.setOnCheckedChangeListener { _, isChecked ->
            // Save switch state to SharedPreferences
            sharedPreferences.edit().putBoolean("switch4", isChecked).apply()
        }

        binding.switch5.setOnCheckedChangeListener { _, isChecked ->
            // Save switch state to SharedPreferences
            sharedPreferences.edit().putBoolean("switch5", isChecked).apply()
        }


        binding.button2.setOnClickListener {
            goToMyPageActivity()
        }

    }


    private fun goToMyPageActivity() {
        val intent = Intent(activity, MyPageActivity::class.java)
        startActivity(intent)
    }




}
