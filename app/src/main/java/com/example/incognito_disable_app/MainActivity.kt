package com.example.incognito_disable_app

import android.annotation.SuppressLint
//import android.app.AppOpsManager
//import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
//import android.app.usage.UsageStats
import android.content.ComponentName
//import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.provider.Settings
//import android.app.usage.UsageStatsManager
//import android.os.Build
//import androidx.appcompat.widget.SwitchCompat


class MainActivity : AppCompatActivity() {

    // Declaring variables for UI components
    private lateinit var tvStatus: TextView
    private lateinit var switchIncognito: Switch
    private lateinit var btnSettings: Button

    // Hardcoded password for disabling the blocker
    private val correctPassword = "1234" // You can change this

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI components
        tvStatus = findViewById(R.id.tvStatus)
        switchIncognito = findViewById(R.id.switchIncognito)
        btnSettings = findViewById(R.id.btnSettings)

        // Set up the initial state of the incognito mode
        updateStatus(switchIncognito.isChecked)

        // Toggle incognito mode when the switch is changed
        switchIncognito.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) {
                // Show the password dialog if the user tries to disable incognito mode
                showPasswordDialog()
            } else {
                // Incognito mode is enabled
                updateStatus(true)
            }
        }
        // Register the ActivityResultLauncher
        val enableAdminLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    // Admin privileges granted
                    Toast.makeText(this, "Device admin enabled", Toast.LENGTH_SHORT).show()
                } else {
                    // Admin privileges denied
                    Toast.makeText(this, "Device admin not enabled", Toast.LENGTH_SHORT).show()
                }
            }

        val enableAdminButton: Button =
            findViewById(R.id.enableAdminButton) // Replace with your button ID
        enableAdminButton.setOnClickListener {
            // Request device admin privileges
            val adminComponent = ComponentName(this, MyAdminReceiver::class.java)
            //val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
                putExtra(
                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "This app requires admin privileges to prevent uninstallation."
                )
            }
            enableAdminLauncher.launch(intent) // Use the launcher to start the activity
        }
        // Settings button functionality
        btnSettings.setOnClickListener {
            // Open device admin settings
            val intent = Intent(Settings.ACTION_SECURITY_SETTINGS)
            startActivity(intent)
        }

       /* fun hasUsageStatsPermission(): Boolean {
            val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(),
                    packageName
                )
            } else {
                appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(),
                    packageName
                )
            }
            return mode == AppOpsManager.MODE_ALLOWED
        }*/

       /* fun isIncognitoDetected(usageStats: UsageStats?): Boolean {

            // Placeholder method to check for patterns that may indicate incognito mode
            // You could enhance this by tracking browser behavior or checking for specific activities
            // In Chrome, incognito usage may reflect in certain activity names or timestamps
            usageStats?.let {
                return it.lastTimeUsed > (System.currentTimeMillis() - 1000 * 60) // Safe call
            }
            return false // Example check for recent use
        }

        fun blockBrowser(packageName: String?) {
            // Use DevicePolicyManager to block the app or close it
            // You can force-stop the browser or restrict its use
            val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

            try {
                dpm.clearPackagePersistentPreferredActivities(
                    componentName,
                    packageName
                ) // Example of blocking or clearing preferences
                Toast.makeText(this, "$packageName blocked!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Failed to block $packageName", Toast.LENGTH_SHORT).show()
            }
        } */

      /*  fun monitorIncognitoMode() {
            // Get the UsageStatsManager system service

            val usageStatsManager =
                getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val time = System.currentTimeMillis()

            // Get app usage data for the last minute
            val appList = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                time - 1000 * 60,
                time
            )

            if (appList != null && appList.isNotEmpty()) {
                for (usageStats in appList) {
                    val packageName = usageStats.packageName

                    // Check for browser packages like Chrome or Firefox
                    if (packageName.contains("com.android.chrome") || packageName.contains("org.mozilla.firefox")) {
                        // Check for specific conditions that might indicate incognito mode
                        if (isIncognitoDetected(usageStats)) {
                            // Display a message or take action (e.g., block or close the browser)
                            Toast.makeText(
                                this,
                                "Incognito Mode Detected in $packageName",
                                Toast.LENGTH_SHORT
                            ).show()
                            blockBrowser(packageName) // Custom method to block or close browser
                        }
                    }
                }
            }
        }*/


    } //oncreate ends


    // Function to update the status TextView
    private fun updateStatus(isBlocked: Boolean) {
        if (isBlocked) {
            tvStatus.text = getString(R.string.incognito_disabled)
            tvStatus.setTextColor(getColor(android.R.color.holo_red_dark))
        } else {
            tvStatus.text =getString(R.string.incognito_enabled)
            tvStatus.setTextColor(getColor(android.R.color.holo_green_dark))
        }
    }

    // Function to show a password dialog
    private fun showPasswordDialog() {
        // Create an alert dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Password Required")

        // Set up the input
        val input = EditText(this)
        input.hint = "Enter password"
        builder.setView(input)

        // Set up the buttons
        builder.setPositiveButton("OK") { dialog, _ ->
            val enteredPassword = input.text.toString()
            if (enteredPassword == correctPassword) {
                // Correct password entered, disable incognito mode
                updateStatus(false)
                switchIncognito.isChecked = false
            } else {
                // Incorrect password entered, keep the switch checked
                switchIncognito.isChecked = true
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            // Cancel action, keep the switch checked
            switchIncognito.isChecked = true
            dialog.cancel()
        }

        builder.show()
    }

} // class ends







