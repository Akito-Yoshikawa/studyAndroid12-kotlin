package com.example.roomdemo

import android.app.AlertDialog
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.roomdemo.databinding.ActivityMainBinding
import com.example.roomdemo.databinding.DialogUpdateBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        val employeeDao = (applicationContext as EmployeeApp).db.employeeDao()
        binding?.AddRecordBtn?.setOnClickListener {
            addRecord(employeeDao)
        }

        lifecycleScope.launch {
            employeeDao.fetchAllEmployees().collect {
                val list = ArrayList(it)
                setupListOfDataIntoRecyclerView(list, employeeDao)
            }
        }
    }

    private fun addRecord(employeeDao: EmployeeDao) {
        val name = binding?.etName?.text.toString()
        val email = binding?.etEmail?.text.toString()

        if (name.isNotEmpty() && email.isNotEmpty()) {
            lifecycleScope.launch {
                employeeDao.insert(EmployeeEntity(name = name, email = email))
                Toast.makeText(applicationContext, "Record saved", Toast.LENGTH_LONG).show()

                binding?.etName?.text?.clear()
                binding?.etEmail?.text?.clear()
            }
        } else {
            Toast.makeText(applicationContext, "Name or Email cannot be blank", Toast.LENGTH_LONG).show()
        }
    }
    private fun setupListOfDataIntoRecyclerView(employeesList:ArrayList<EmployeeEntity>,
    employeeDao: EmployeeDao) {
        if (employeesList.isNotEmpty()) {
            val itemAdapter = ItemAdapter(employeesList,
                {
                    // クロージャー ItemAdapterから受け取ったupdateIDをupdateRecordDialogに渡して処理
                    updateID ->
                    updateRecordDialog(updateID, employeeDao)
                },
                {
                    // クロージャー ItemAdapterから受け取ったdeleteIDをdeleteRecordAlertDialogに渡して処理
                        deleteID ->
                    deleteRecordAlertDialog(deleteID, employeeDao)
                })

            binding?.rvItemsList?.layoutManager = LinearLayoutManager(this)
            binding?.rvItemsList?.adapter = itemAdapter
            binding?.rvItemsList?.visibility = View.VISIBLE
        } else {
            binding?.rvItemsList?.visibility = View.GONE
            binding?.tvNoRecordsAvailable?.visibility = View.VISIBLE

        }
    }

    private fun updateRecordDialog(id: Int, employeeDao: EmployeeDao) {
        // CustomDialog
        // R.style.Theme_Dialog参照できず困った。。 対処：import androidx.appcompat.Rを消す
        val updateDialog = Dialog(this, R.style.Theme_Dialog)
        updateDialog.setCancelable(false)
        val binding = DialogUpdateBinding.inflate(layoutInflater)
        updateDialog.setContentView(binding.root)

        lifecycleScope.launch {
            employeeDao.fetchEmployeeById(id).collect {
                // クラッシュ回避
                if (it != null) {
                    binding.etName.setText(it.name)
                    binding.etEmail.setText(it.email)
                }
            }
        }

        // updateボタンクリック時アクション
        binding.updateBtn.setOnClickListener {
            val name = binding.etName.text.toString()
            val email = binding.email.text.toString()

            // nameとemailが空かどうかチェック
            if (name.isNotEmpty() && email.isNotEmpty()) {
                lifecycleScope.launch {
                    employeeDao.update(EmployeeEntity(id, name = name, email = email))
                   Toast.makeText(applicationContext, "Record Updated.", Toast.LENGTH_LONG).show()
                    updateDialog.dismiss()
                }
            } else {
                Toast.makeText(applicationContext, "Name or Email cannot be blank", Toast.LENGTH_LONG).show()
            }
        }

        // cancelボタンクリック時アクション
        binding.cancelBtn.setOnClickListener {
            updateDialog.dismiss()
        }

        // 表示
        updateDialog.show()
    }

    private fun deleteRecordAlertDialog(id: Int, employeeDao: EmployeeDao) {
        // DefaultDialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Record")
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        // Yesボタンクリック時アクション
        builder.setPositiveButton("Yes") { dialogInterface, _ ->
            lifecycleScope.launch {
                employeeDao.delete(EmployeeEntity(id))
                Toast.makeText(applicationContext, "Record deleted successfully.", Toast.LENGTH_LONG).show()
            }
            dialogInterface.dismiss()
        }

        // Noボタンクリック時アクション
        builder.setNegativeButton("No") { dialogInterface, _ ->
              dialogInterface.dismiss()
        }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }
}