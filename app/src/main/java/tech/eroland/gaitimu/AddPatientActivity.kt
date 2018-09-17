package tech.eroland.gaitimu

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_add_patient.*
import android.widget.RadioButton
import android.widget.Toast


class AddPatientActivity : AppCompatActivity() {
    private var gender:Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_patient)

        setSupportActionBar(toolbar)
        supportActionBar?.setTitle(R.string.add_patient)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        btn_add_patient.setOnClickListener {
            val name:String = input_name.text.toString()
            val age:Int = input_age.text.toString().toInt()
            val location:String = input_location.text.toString()
            val observations:String = input_observations.text.toString()
            val patient = Patient(name, age, gender, location, observations)
            val dbManager = DBManager(this)
            val values = patient.toContentValues()

            val id = dbManager.insert(values)
            if(id > 0){
                Toast.makeText(this, " paciente agregado", Toast.LENGTH_LONG).show()
            }else{
                Toast.makeText(this, " paciente no agregado", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun onRadioGenderClicked(view: View) {
        // Is the button now checked?
        val checked = (view as RadioButton).isChecked
        // Check which radio button was clicked
        when (view.getId()) {
            R.id.radio_male -> if (checked)
                gender = 1
            R.id.radio_female -> if (checked)
                gender = 2
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
