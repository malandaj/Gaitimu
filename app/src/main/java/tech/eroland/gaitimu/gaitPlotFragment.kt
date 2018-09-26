package tech.eroland.gaitimu

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jjoe64.graphview.series.DataPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_gait_plot.view.*
import okhttp3.*
import org.json.JSONObject
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.android.synthetic.main.chart.view.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [gaitPlotFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [gaitPlotFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class gaitPlotFragment : Fragment() {
    private var ws:WebSocket?=null
    private val NORMAL_CLOSURE_STATUS = 1000
    private var ip:String?=null
    private var port:Int?=null
    private var nSensors:Int?=null
    private var patient:String ?= null
    private var mHandler = Handler()
    private var sensors:ArrayList<MySensor>?=null


    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            patient = it.getString("patient")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_gait_plot, container, false)
        val serverSettings = ServerSettings(activity!!.applicationContext)
        val values = serverSettings.toContentValues()
        ip = values.getAsString(this.getString(R.string.wsIP))
        port = values.getAsInteger(this.getString(R.string.wsPort))
        nSensors = values.getAsInteger(this.getString(R.string.nSensors)).toInt()

        sensors = ArrayList()
        for(i in 0 until nSensors!!){
            sensors!!.add(i, MySensor(i, activity!!.applicationContext, view))
        }

        view.button_empezar.setOnClickListener {
            starRegister()
        }

        view.button_terminar.setOnClickListener {
            val jsonObject = JSONObject()
            jsonObject.put("type", "stopRecording")
            jsonObject.put("name", activity!!.toolbar.title)
            ws!!.send(jsonObject.toString())

//            jsonObject = JSONObject()
//            jsonObject.put("type", "saveZip")
//            jsonObject.put("name", activity!!.toolbar.title)
//            ws!!.send(jsonObject.toString())

            ws!!.close(NORMAL_CLOSURE_STATUS, "Goodbye !")
        }
        return view
    }

    private fun starRegister(){
        val client = OkHttpClient()
        val request = Request.Builder().url("http://$ip:$port").addHeader("Sec-WebSocket-Protocol", "androidclient").build()
        val wsListener = webSocketListener()
        ws = client.newWebSocket(request, wsListener)
        client.dispatcher().executorService().shutdown()
        ws!!.send(patient!!)
        val jsonObject = JSONObject()
        jsonObject.put("type", "startRecording")
        ws!!.send(jsonObject.toString())
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment gaitPlotFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                gaitPlotFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }

    private inner class webSocketListener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            val jsonObject = JSONObject(text)
            val id = (jsonObject.getString("ID").toInt()) - 1
            val lectures = jsonObject.getJSONArray("lectures")
//            val accXIndex = arrayOf(0, 8, 16, 24, 32)
//            val accYIndex = arrayOf(1, 9, 17, 25, 33)
//            val accZIndex = arrayOf(2, 10, 18, 26, 34)
//            val gyroXIndex = arrayOf(3, 11, 19, 27, 35)
//            val gyroYIndex = arrayOf(4, 12, 20, 28, 36)
//            val gyroZIndex = arrayOf(5, 13, 21, 29, 37)

            mHandler.post {
                sensors!![id].graphLastXValue += 5.0
                if(sensors!![id].graphLastXValue.rem(10.0) == 0.0){
                    sensors!![id].updateSeries(sensors!![id].graphLastXValue, arrayOf(
                            lectures.getInt(0),
                            lectures.getInt(1),
                            lectures.getInt(2),
                            lectures.getInt(3),
                            lectures.getInt(4),
                            lectures.getInt(5)))
                }
            }
            super.onMessage(webSocket, text)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            println(t.message)
            super.onFailure(webSocket, t, response)
        }
    }

    private inner class MySensor{
        private var id:Int = 0
        private var dataSeries:ArrayList<LineGraphSeries<DataPoint>> = ArrayList()
        private var context:Context?=null
        private var view:View?=null
        private var titles:Array<String> = arrayOf("accX", "accY", "accZ", "gyroX", "gyroY", "gyroZ")
        private var colors:Array<Int> = arrayOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.BLACK, Color.MAGENTA)
        var graphLastXValue:Double = 0.0
        private var names:Array<String> = arrayOf("Brazo Derecho", "Brazo Izquierdo", "Pierna Derecha", "Pierna Izquierda", "Espalda")

        constructor(id:Int, context: Context, view: View){
            this.id = id
            this.context = context
            this.view = view

            addplot()
        }

        private fun addplot(){
            val chartView = layoutInflater.inflate(R.layout.chart, null)
            chartView.plot.title = "Sensor ${this.id.plus(1)} - ${names[this.id]}"
            chartView.plot.viewport.isXAxisBoundsManual = true
            chartView.plot.viewport.setMinX(0.0)
            chartView.plot.viewport.setMaxX(300.0)
            for(j in 0 until 6){
                dataSeries.add(j, LineGraphSeries())
                dataSeries[j].title = titles[j]
                dataSeries[j].color = colors[j]
                chartView.plot.addSeries(dataSeries[j])
            }
            view!!.chartLayout.addView(chartView)
        }

        fun updateSeries(xIndex:Double, values:Array<Int>){
            for(j in 0 until 6){
                val dataPoint = DataPoint(xIndex, values[j].toDouble())
                dataSeries[j].appendData(dataPoint, true, 300)
            }
        }
    }
}
