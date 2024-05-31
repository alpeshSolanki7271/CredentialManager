package com.example.passwordmanager.ui

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.example.passwordmanager.R
import com.example.passwordmanager.data.model.PasswordData
import com.example.passwordmanager.databinding.ItemAccountDetailsLayoutBinding
import com.example.passwordmanager.util.AlarmReceiver
import com.example.passwordmanager.util.AppConstants
import com.example.passwordmanager.util.AppConstants.Companion.decrypt
import com.example.passwordmanager.util.AppConstants.Companion.encrypt
import com.example.passwordmanager.util.BiometricHelper
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ModalBottomSheetDialogDetails(
    val passwordData: PasswordData,
    private val viewModel: PassViewModel,
    val onItemDelete: (position: Int) -> Unit,
    val onItemEdit: (position: Int) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var binding: ItemAccountDetailsLayoutBinding
    private lateinit var biometricHelper: BiometricHelper
    private val calendar = Calendar.getInstance()
    private lateinit var adapter: LogoAdapter
    private var selectedImage = 0
    private var dateString = ""

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = ItemAccountDetailsLayoutBinding.inflate(inflater, container, false)

        biometricHelper = BiometricHelper(requireContext())
        binding.tvAccountType.text = passwordData.accountType
        binding.tvUsername.text = passwordData.username

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


        if (dateString.isNotBlank()) {
            val calendar = AppConstants.parseDateString(dateString)
            setAlarm(requireContext(), calendar)
        }

        binding.etPassword.addTextChangedListener(object : TextWatcher {
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

        binding.etPasswordLayout.setEndIconOnClickListener {
            val editText = binding.etPasswordLayout.editText
            if (editText != null) {
                val inputType = editText.inputType
                val isPasswordVisible =
                    inputType and InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                if (isPasswordVisible) {
                    editText.inputType =
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                    editText.setSelection(editText.text?.length ?: 0)

                } else {
                    showBiometricPromptForPasswordToggle(editText)
                }
            }
        }

        binding.date.setOnClickListener {
            showDatePicker()
        }

        binding.editBtn.setOnClickListener {
            if (binding.editBtn.text == "Edit") {
                binding.editBtn.text = resources.getString(R.string.update)
                binding.layoutEdit.visibility = View.VISIBLE
                binding.layoutShow.visibility = View.GONE
                binding.edName.setText(passwordData.accountType)
                binding.email.setText(passwordData.username)
                binding.date.text = passwordData.date
                val decryptPassword = decrypt(passwordData.password, "ASDFGHJKLASDFGHJ")
                binding.etPassword.setText(decryptPassword)
            } else {
                binding.editBtn.text = resources.getString(R.string.edit)

                if (binding.edName.text!!.isBlank()) {
                    Toast.makeText(
                        requireContext(), "Please enter account name", Toast.LENGTH_SHORT
                    ).show()
                } else if (binding.email.text!!.isBlank()) {
                    Toast.makeText(
                        requireContext(), "Please enter username or email", Toast.LENGTH_SHORT
                    ).show()
                } else if (binding.etPassword.text!!.isBlank()) {
                    Toast.makeText(requireContext(), "Please enter password", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    dateString = binding.date.text.toString()
                    val accountName = binding.edName.text.toString()
                    val email = binding.email.text.toString()
                    val password = binding.etPassword.text.toString()

                    val encryptedPass = encrypt(text = password, secretKey = "ASDFGHJKLASDFGHJ")

                    val passwordData = PasswordData(
                        id = passwordData.id,
                        accountType = accountName,
                        username = email,
                        password = encryptedPass,
                        logo = selectedImage,
                        date = dateString
                    )

                    CoroutineScope(Dispatchers.IO).launch {
                        viewModel.updateData(passwordData = passwordData)
                        activity?.runOnUiThread {
                            Toast.makeText(
                                requireContext(), "Account details updated", Toast.LENGTH_SHORT
                            ).show()
                            binding.edName.text!!.clear()
                            binding.email.text!!.clear()
                            binding.etPassword.text!!.clear()
                            dismiss()
                            onItemEdit(passwordData.id)
                        }
                    }
                }
            }
        }

        binding.deleteBtn.setOnClickListener {
            val dialog =
                AlertDialog.Builder(requireContext()).setTitle("Delete account detail").setMessage("Are you sure you want to delete this account?")
                    .setPositiveButton("Yes") { dialogInterface, _ ->
                        CoroutineScope(Dispatchers.IO).launch {
                            viewModel.deleteData(id = passwordData.id)
                            activity?.runOnUiThread {
                                Toast.makeText(
                                    requireContext(), "Account Deleted", Toast.LENGTH_SHORT
                                ).show()
                                dismiss()
                                onItemDelete(passwordData.id)
                            }
                        }
                        dialogInterface.dismiss()
                    }.setNegativeButton("No") { dialogInterface, _ ->
                        dialogInterface.dismiss()
                    }.create()

            dialog.show()
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
        datePickerDialog.show()
    }

    private fun togglePasswordVisibility() {
        val inputType = binding.etPassword.inputType
        binding.etPassword.inputType =
            if (inputType == android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            } else {
                android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            }
        binding.etPassword.text?.let { binding.etPassword.setSelection(it.length) }
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


    @SuppressLint("ShowToast")
    private fun showBiometricPromptForPasswordToggle(editText: EditText) {
        if (biometricHelper.isBiometricAvailable()) {
            biometricHelper.showBiometricPrompt(requireActivity(), {
                editText.isCursorVisible = true
                togglePasswordVisibility()
            }, { error ->
                editText.isCursorVisible = false
                Log.e("TAG", "showBiometricPromptForPasswordToggle: $error")
                val snackBar = Snackbar.make(binding.root, error, Snackbar.LENGTH_SHORT)
                snackBar.show()
                // Handle biometric authentication error
            })
        } else if (biometricHelper.isDeviceCredentialAvailable()) {
            biometricHelper.showDeviceCredentialPrompt(requireActivity(), {
                editText.isCursorVisible = true
                togglePasswordVisibility()
            }, { error ->
                editText.isCursorVisible = false
                Log.e("TAG", "showBiometricPromptForPasswordToggle: $error")
                val snackBar = Snackbar.make(binding.root, error, Snackbar.LENGTH_SHORT)
                snackBar.show()
                // Handle device credential authentication error
            })
        } else {
            // Fallback if biometrics and device credentials are not available
            editText.isCursorVisible = true
            togglePasswordVisibility()
        }
    }
}