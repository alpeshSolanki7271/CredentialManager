package com.example.passwordmanager.ui

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Insets.add
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatTextView
import com.example.passwordmanager.R
import com.example.passwordmanager.data.model.PasswordData
import com.example.passwordmanager.databinding.ItemBottomsheetBinding
import com.example.passwordmanager.util.AlarmReceiver
import com.example.passwordmanager.util.AppConstants
import com.example.passwordmanager.util.AppConstants.Companion.parseDateString
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Base64
import java.util.Calendar
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class ModalBottomSheetDialog(private val passViewModel: PassViewModel) :
    BottomSheetDialogFragment() {

    private lateinit var binding: ItemBottomsheetBinding
    private val calendar = Calendar.getInstance()
    private lateinit var adapter: LogoAdapter
    private var selectedImage = 0
    private var dateString = ""

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = ItemBottomsheetBinding.inflate(inflater, container, false)
        binding.strengthTv.visibility = View.VISIBLE


        val imageList = ArrayList<Int>()
        imageList.apply {
            add(R.drawable.google)
            add(R.drawable.amazon)
            add(R.drawable.linkedin)
            add(R.drawable.insta)
            add(R.drawable.facebook)
            add(R.drawable.github)
            add(R.drawable.twitter)
            add(R.drawable.snapchat)
            add(R.drawable.skype)
        }
        adapter = LogoAdapter(context = requireContext(), logoList = imageList, onItemClicked = {
            selectedImage = it
        })
        binding.logoRv.adapter = adapter


        if (dateString.isNotBlank()){
            val calendar = parseDateString(dateString)
            setAlarm(requireContext(), calendar)
        }

        binding.password.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?, start: Int, count: Int, after: Int
            ) {

            }

            override fun onTextChanged(
                s: CharSequence?, start: Int, before: Int, count: Int
            ) {
                binding.strengthTv.visibility = View.VISIBLE
                val password = s.toString()
                val strength = AppConstants.calculatePasswordStrength(password)
                AppConstants.displayPasswordStrength(strength, binding.strengthTv, requireContext())
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

        binding.addButton.setOnClickListener {
            dateString = binding.date.text.toString()
            if (binding.edName.text!!.isBlank()) {
                Toast.makeText(requireContext(), "Please enter account name", Toast.LENGTH_SHORT)
                    .show()
            } else if (binding.email.text!!.isBlank()) {
                Toast.makeText(
                    requireContext(), "Please enter username or email", Toast.LENGTH_SHORT
                ).show()
            } else if (binding.password.text!!.isBlank()) {
                Toast.makeText(requireContext(), "Please enter password", Toast.LENGTH_SHORT).show()
            } else {

                val accountName = binding.edName.text.toString()
                val email = binding.email.text.toString()
                val password = binding.password.text.toString()

                val encryptedPass =
                    AppConstants.encrypt(text = password, secretKey = "ASDFGHJKLASDFGHJ")

                val passwordData = PasswordData(
                    accountType = accountName,
                    username = email,
                    password = encryptedPass,
                    logo = selectedImage,
                    date = dateString
                )

                CoroutineScope(Dispatchers.IO).launch {
                    passViewModel.addAccount(passwordData = passwordData)
                    activity?.runOnUiThread {
                        Toast.makeText(requireContext(), "Account Added", Toast.LENGTH_SHORT).show()
                        binding.edName.text!!.clear()
                        binding.email.text!!.clear()
                        binding.password.text!!.clear()
                        dismiss()
                        passViewModel.fetchUserList()
                    }
                }
            }
        }


        binding.date.setOnClickListener {
            showDatePicker()
        }


        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // used to show the bottom sheet dialog
        dialog?.setOnShowListener { it ->
            val d = it as BottomSheetDialog
            val bottomSheet =
                d.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        return super.onCreateDialog(savedInstanceState)
    }


    @SuppressLint("SetTextI18n")
    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, monthOfYear, dayOfMonth)
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val formattedDate = dateFormat.format(selectedDate.time)
                binding.date.text = formattedDate
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.minDate = calendar.timeInMillis
        datePickerDialog.show()
    }

    @SuppressLint("ScheduleExactAlarm")
    fun setAlarm(context: Context, calendar: Calendar) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }


}