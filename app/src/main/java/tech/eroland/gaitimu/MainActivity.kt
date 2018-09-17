package tech.eroland.gaitimu

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem
import kotlinx.android.synthetic.main.activity_main.*
import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.IProfile
import android.content.Intent
import android.net.Uri
import com.mikepenz.materialdrawer.model.interfaces.Nameable
import android.support.v4.app.Fragment;

class MainActivity : AppCompatActivity(), gaitPlotFragment.OnFragmentInteractionListener {
    private var dbManager:DBManager?=null
    private var headerResult: AccountHeader? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        dbManager = DBManager(this)
        setupDrawerContent()
    }

    override fun onResume() {
        super.onResume()
        setHeaderProfiles()
    }

    private fun setupDrawerContent(){
        val profiles = loadProfiles("%")
        headerResult = AccountHeaderBuilder()
                    .withActivity(this)
                    .withHeaderBackground(R.drawable.drawer_background)
                    .withProfiles(profiles)
                    .withOnAccountHeaderListener(AccountHeader.OnAccountHeaderListener { _, profile, _ ->
                        selectDrawerHeaderItem(profile)
                        return@OnAccountHeaderListener true
                    })
                    .build()

        DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withActionBarDrawerToggleAnimated(true)
                .withAccountHeader(headerResult!!)
                .inflateMenu(R.menu.drawer_menu)
                .withSelectedItem(-1)
                .withOnDrawerItemClickListener { _, _, drawerItem ->
                    selectDrawerItem(drawerItem)
                    return@withOnDrawerItemClickListener false
                }
                .build()
    }

    private fun setHeaderProfiles(){
        val profiles = loadProfiles("%")
        headerResult!!.profiles = profiles
    }

    private fun selectDrawerHeaderItem(profile: IProfile<*>) {
        if (profile is IDrawerItem<*, *> && profile.getIdentifier() == 10000L) {
            val intent = Intent(this, AddPatientActivity::class.java)
            startActivity(intent)
        }
    }

    private fun selectDrawerItem(drawerItem: IDrawerItem<*, *>){
        var fragment:Fragment?=null
        val fragmentClass = gaitPlotFragment::class.java
        if (drawerItem is Nameable<*>) {
            val title = (drawerItem as Nameable<*>).name.getText(this)
            when (title){
                this.getString(R.string.config_str) -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                }
                this.getString(R.string.marcha_str),
                this.getString(R.string.pos_str),
                this.getString(R.string.per_dedos_str),
                this.getString(R.string.dedo_nariz_str),
                this.getString(R.string.mov_alter_str),
                this.getString(R.string.tobillo_str) -> {
                    return
                }
                else -> {
                    try {
                        toolbar.title = title
                        fragment = (fragmentClass.newInstance() as Fragment)
                        val patient = getPatient(headerResult!!.activeProfile.name.toString())
                        val bundle = Bundle()
                        bundle.putString("patient", patient.toJSON().toString())
                        fragment!!.arguments = bundle
                    }catch (e:Exception) {
                        e.printStackTrace();
                    }
                    val fragmentManager = supportFragmentManager
                    fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit()
                }
            }
        }
    }

    private fun loadProfiles(title:String):ArrayList<IProfile<*>>{
        val profiles = ArrayList<IProfile<*>>()
        val projection = arrayOf("ID", "Name")
        val selectionArgs = arrayOf(title)
        val cursor = dbManager!!.query(projection, "Name like ?", selectionArgs, "ID")
        if(cursor.moveToFirst()){
            do {
                val id = cursor.getString(cursor.getColumnIndex("ID"))
                val name = cursor.getString(cursor.getColumnIndex("Name"))
                profiles.add(ProfileDrawerItem().withName(name).withIdentifier(id.toLong()))
            }while (cursor.moveToNext())
        }
        profiles.add(ProfileSettingDrawerItem().withName("Agregar paciente").withDescription("Agregar nuevo paciente").withIdentifier(10000))
        return profiles
    }

    override fun onFragmentInteraction(uri: Uri) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun getPatient(name:String): Patient{
        var currentPatient:Patient ?= null
        val projection = arrayOf("Name", "Age", "Gender", "Location", "Observations")
        val selectionArgs = arrayOf(name)
        val cursor = dbManager!!.query(projection, "Name like ?", selectionArgs, "Name")
        if(cursor.moveToFirst()){
            do {
                val name = cursor.getString(cursor.getColumnIndex("Name"))
                val age = cursor.getInt(cursor.getColumnIndex("Age"))
                val gender = cursor.getInt(cursor.getColumnIndex("Gender"))
                val location = cursor.getString(cursor.getColumnIndex("Location"))
                val observations = cursor.getString(cursor.getColumnIndex("Observations"))
                currentPatient = Patient(name, age, gender, location, observations)
            }while (cursor.moveToNext())
        }
        return currentPatient!!
    }
}