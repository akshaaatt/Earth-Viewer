package com.aemerse.earth_viewer

import android.app.Activity
import android.app.AlertDialog
import android.os.AsyncTask
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.aemerse.earth_viewer.R
import java.util.*

class EarthActivity : Activity() {
    private val ID_MENU_TIME_RESET = 1
    private val ID_MENU_LIGHT = 2
    private val ID_MENU_LIGHT_SPECULAR = 50
    private val ID_MENU_PLAY = 3
    private val ID_MENU_COPYRIGHT = 4
    private val ID_MENU_IMAGERY_AIRMASS = 5
    private val ID_MENU_IMAGERY_INFRARED = 7
    private val ID_MENU_IMAGERY_VISIBLE_INFRARED = 8
    private val ID_MENU_IMAGERY_WATER_VAPOR = 9
    private val ID_MENU_IMAGERY_MPE = 10
    private val ID_MENU_IMAGERY_IODC = 11
    private val ID_MENU_IMAGERY_XPLANET = 12
    private val ID_MENU_IMAGERY_SSEC_IR = 13
    private val ID_MENU_IMAGERY_SSEC_WATER = 14
    private val ID_MENU_IMAGERY_GOES_EAST = 15
    private val ID_MENU_IMAGERY_GOES_WEST = 16
    private val ID_MENU_IMAGERY_MTSAT = 17
    private val ID_MENU_IMAGERY_CCI_CLOUDS = 18
    private val ID_MENU_IMAGERY_CCI_TEMP = 19
    private val ID_MENU_IMAGERY_CCI_TEMP_AN = 20
    private val ID_MENU_IMAGERY_CCI_WATER = 21
    private val ID_MENU_IMAGERY_CCI_WIND = 22
    private val ID_MENU_IMAGERY_CCI_JET = 23
    private val ID_MENU_IMAGERY_CCI_SNOW = 24
    private val ID_MENU_IMAGERY_CCI_OISST_V2 = 25
    private val ID_MENU_IMAGERY_CCI_OISST_V2_1Y = 26
    private val ID_MENU_IMAGERY_CCI_TEMP_AN_1Y = 27
    private val ID_MENU_IMAGERY_CCI_ERSST_V5 = 28
    private val ID_MENU_IMAGERY_AIRMASS_HD = 30
    private val ID_MENU_IMAGERY_MPE_HD = 31
    private val ID_MENU_IMAGERY_NRL_RAINRATE = 41
    private var mGLView: OpenGLES20SurfaceView? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity
        mGLView = OpenGLES20SurfaceView(this)
        setContentView(mGLView)
        mGLView!!.openGLES20Renderer!!.mEpoch = Calendar.getInstance().timeInMillis
        mGLView!!.openGLES20Renderer!!._e1 = 0L
        mGLView!!.openGLES20Renderer!!._e2 = 0L
        mGLView!!.openGLES20Renderer!!._e3 = 0L
        mGLView!!.openGLES20Renderer!!._e4 = 0L
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(Menu.NONE, ID_MENU_PLAY, Menu.NONE, R.string.play)
        menu.add(Menu.NONE, ID_MENU_TIME_RESET, Menu.NONE, R.string.reset)
        menu.add(Menu.NONE, ID_MENU_LIGHT, Menu.NONE, R.string.light)
        menu.add(Menu.NONE, ID_MENU_LIGHT_SPECULAR, Menu.NONE, R.string.light_specular)
        //menu.add(Menu.NONE, ID_MENU_IMAGERY_XPLANET, Menu.NONE, R.string.xplanet);
        menu.add(Menu.NONE, ID_MENU_IMAGERY_CCI_CLOUDS, Menu.NONE, R.string.cci_clouds)
        menu.add(Menu.NONE, ID_MENU_IMAGERY_CCI_TEMP, Menu.NONE, R.string.cci_temp)
        menu.add(Menu.NONE, ID_MENU_IMAGERY_CCI_TEMP_AN, Menu.NONE, R.string.cci_temp_an)
        menu.add(Menu.NONE, ID_MENU_IMAGERY_CCI_WATER, Menu.NONE, R.string.cci_water)
        menu.add(Menu.NONE, ID_MENU_IMAGERY_CCI_WIND, Menu.NONE, R.string.cci_wind)
        menu.add(Menu.NONE, ID_MENU_IMAGERY_CCI_JET, Menu.NONE, R.string.cci_jet)
        menu.add(Menu.NONE, ID_MENU_IMAGERY_CCI_TEMP_AN_1Y, Menu.NONE, R.string.cci_temp_an_1y)
        menu.add(Menu.NONE, ID_MENU_IMAGERY_CCI_OISST_V2_1Y, Menu.NONE, R.string.cci_oisst_v2_1y)
        menu.add(Menu.NONE, ID_MENU_IMAGERY_CCI_OISST_V2, Menu.NONE, R.string.cci_oisst_v2)
        menu.add(Menu.NONE, ID_MENU_IMAGERY_CCI_ERSST_V5, Menu.NONE, R.string.cci_ersst_v5)
        //menu.add(Menu.NONE, ID_MENU_IMAGERY_CCI_SNOW, Menu.NONE, R.string.cci_snow);
        //menu.add(Menu.NONE, ID_MENU_IMAGERY_NRL_RAINRATE, Menu.NONE, R.string.nrl_rainrate);
        menu.add(Menu.NONE, ID_MENU_IMAGERY_AIRMASS, Menu.NONE, R.string.meteosat_0_airmass)
        menu.add(Menu.NONE, ID_MENU_IMAGERY_AIRMASS_HD, Menu.NONE, R.string.meteosat_0_airmass_hd)
        menu.add(Menu.NONE, ID_MENU_IMAGERY_MPE, Menu.NONE, R.string.meteosat_0_mpe)
        menu.add(Menu.NONE, ID_MENU_IMAGERY_INFRARED, Menu.NONE, R.string.meteosat_0_infrared)
        menu.add(Menu.NONE, ID_MENU_IMAGERY_IODC, Menu.NONE, R.string.meteosat_iodc_mpe)
        //menu.add(Menu.NONE, ID_MENU_IMAGERY_GOES_EAST, Menu.NONE, R.string.goes_east);
        //menu.add(Menu.NONE, ID_MENU_IMAGERY_GOES_WEST, Menu.NONE, R.string.goes_west);
        //menu.add(Menu.NONE, ID_MENU_IMAGERY_MTSAT, Menu.NONE, R.string.mtsat);
        menu.add(Menu.NONE, ID_MENU_IMAGERY_SSEC_IR, Menu.NONE, R.string.ssec_ir)
        menu.add(Menu.NONE, ID_MENU_IMAGERY_SSEC_WATER, Menu.NONE, R.string.ssec_water)
        menu.add(Menu.NONE, ID_MENU_COPYRIGHT, Menu.NONE, "Copyright")
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPause() {
        super.onPause()
        // The following call pauses the rendering thread.
        // If your OpenGL application is memory intensive,
        // you should consider de-allocating objects that
        // consume significant memory here.
        mGLView!!.onPause()
    }

    override fun onResume() {
        super.onResume()
        // The following call resumes a paused rendering thread.
        // If you de-allocated graphic objects for onPause()
        // this is a good place to re-allocate them.
        mGLView!!.onResume()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //check selected menu item
        when (item.itemId) {
            ID_MENU_TIME_RESET -> {
                mGLView!!.openGLES20Renderer!!.mTimeRotate = 0
                mGLView!!.openGLES20Renderer!!.mEpoch = Calendar.getInstance().timeInMillis
                mGLView!!.openGLES20Renderer!!._e1 = 0L
                mGLView!!.openGLES20Renderer!!._e2 = 0L
                mGLView!!.openGLES20Renderer!!._e3 = 0L
                mGLView!!.openGLES20Renderer!!._e4 = 0L
            }
            ID_MENU_LIGHT -> {
                when {
                    mGLView!!.openGLES20Renderer!!.mLiveLight -> {
                        mGLView!!.openGLES20Renderer!!.mLiveLight = false
                        mGLView!!.openGLES20Renderer!!.DEV!!.Light[0]!!.Pos =
                            M3DVECTOR(mGLView!!.openGLES20Renderer!!.DEV!!.Light[1]!!.Pos)
                    }
                    else -> {
                        mGLView!!.openGLES20Renderer!!.mLiveLight = true
                        mGLView!!.openGLES20Renderer!!.DEV!!.Light[0]!!.Pos =
                            M3DVECTOR(mGLView!!.openGLES20Renderer!!.DEV!!.Light[2]!!.Pos)
                    }
                }
                return true
            }
            ID_MENU_LIGHT_SPECULAR -> {
                if (mGLView!!.openGLES20Renderer!!.mLightSpecular) {
                    mGLView!!.openGLES20Renderer!!.mLightSpecular = false
                    mGLView!!.openGLES20Renderer!!.DEV!!.Light[2]!!.SR = 0.0f
                    mGLView!!.openGLES20Renderer!!.DEV!!.Light[1]!!.SR =
                        mGLView!!.openGLES20Renderer!!.DEV!!.Light[2]!!.SR
                    mGLView!!.openGLES20Renderer!!.DEV!!.Light[0]!!.SR =
                        mGLView!!.openGLES20Renderer!!.DEV!!.Light[1]!!.SR
                    mGLView!!.openGLES20Renderer!!.DEV!!.Light[2]!!.SG = 0.0f
                    mGLView!!.openGLES20Renderer!!.DEV!!.Light[1]!!.SG =
                        mGLView!!.openGLES20Renderer!!.DEV!!.Light[2]!!.SG
                    mGLView!!.openGLES20Renderer!!.DEV!!.Light[0]!!.SG =
                        mGLView!!.openGLES20Renderer!!.DEV!!.Light[1]!!.SG
                    mGLView!!.openGLES20Renderer!!.DEV!!.Light[2]!!.SB = 0.0f
                    mGLView!!.openGLES20Renderer!!.DEV!!.Light[1]!!.SB =
                        mGLView!!.openGLES20Renderer!!.DEV!!.Light[2]!!.SB
                    mGLView!!.openGLES20Renderer!!.DEV!!.Light[0]!!.SB =
                        mGLView!!.openGLES20Renderer!!.DEV!!.Light[1]!!.SB
                } else {
                    mGLView!!.openGLES20Renderer!!.mLightSpecular = true
                    mGLView!!.openGLES20Renderer!!.DEV!!.Light[2]!!.SR = 1.0f
                    mGLView!!.openGLES20Renderer!!.DEV!!.Light[1]!!.SR =
                        mGLView!!.openGLES20Renderer!!.DEV!!.Light[2]!!.SR
                    mGLView!!.openGLES20Renderer!!.DEV!!.Light[0]!!.SR =
                        mGLView!!.openGLES20Renderer!!.DEV!!.Light[1]!!.SR
                    mGLView!!.openGLES20Renderer!!.DEV!!.Light[2]!!.SG = 0.7f
                    mGLView!!.openGLES20Renderer!!.DEV!!.Light[1]!!.SG =
                        mGLView!!.openGLES20Renderer!!.DEV!!.Light[2]!!.SG
                    mGLView!!.openGLES20Renderer!!.DEV!!.Light[0]!!.SG =
                        mGLView!!.openGLES20Renderer!!.DEV!!.Light[1]!!.SG
                    mGLView!!.openGLES20Renderer!!.DEV!!.Light[2]!!.SB = 0.4f
                    mGLView!!.openGLES20Renderer!!.DEV!!.Light[1]!!.SB =
                        mGLView!!.openGLES20Renderer!!.DEV!!.Light[2]!!.SB
                    mGLView!!.openGLES20Renderer!!.DEV!!.Light[0]!!.SB =
                        mGLView!!.openGLES20Renderer!!.DEV!!.Light[1]!!.SB
                }
                return true
            }
            ID_MENU_PLAY -> {
                mGLView!!.openGLES20Renderer!!._e1 = 0L
                mGLView!!.openGLES20Renderer!!._e2 = 0L
                mGLView!!.openGLES20Renderer!!._e3 = 0L
                mGLView!!.openGLES20Renderer!!._e4 = 0L
                mGLView!!.openGLES20Renderer!!.DEV!!.Light[0]!!.Pos =
                    M3DVECTOR(mGLView!!.openGLES20Renderer!!.DEV!!.Light[2]!!.Pos)
                if (mGLView!!.openGLES20Renderer!!.mPlay) {
                    mGLView!!.openGLES20Renderer!!.mPlay = false
                } else if (!mGLView!!.openGLES20Renderer!!.mPlay) {
                    mGLView!!.openGLES20Renderer!!.mPlay = true
                }
                return true
            }
            ID_MENU_COPYRIGHT -> {
                val dlgAlert = AlertDialog.Builder(this)
                val cal = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"))
                dlgAlert.setMessage(
                    """
        CCI DATA: Data obtained using Climate Reanalyzer (http://cci-reanalyzer.org), Climate Change Institute, University of Maine, USA.
        http://pamola.um.maine.edu/DailySummary/frames/GFS-025deg/WORLD-CED/
        
        METEOSAT DATA: All METEOSAT images shown in the application are subject to EUMETSAT copyright. Copyright EUMETSAT ${cal[Calendar.YEAR]}
        http://oiswww.eumetsat.org/IPPS/html/MSG/
        
        SSEC DATA: Provided courtesy of University of Wisconsin-Madison Space Science and Engineering Center
        http://www.ssec.wisc.edu/data/comp/
        
        """.trimIndent()
                )
                dlgAlert.setTitle("Copyright")
                dlgAlert.setPositiveButton(
                    "Ok"
                ) { dialog, which ->
                    //dismiss the dialog
                }
                dlgAlert.setCancelable(true)
                dlgAlert.create().show()
            }
            ID_MENU_IMAGERY_XPLANET -> {
                when {
                    mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.status == AsyncTask.Status.FINISHED -> {
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures =
                            DownloadTexturesXplanet(mGLView!!.openGLES20Renderer)
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.execute()
                    }
                    !mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.isCancelled -> {
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.progressDialogShow()
                    }
                }
            }
            ID_MENU_IMAGERY_NRL_RAINRATE -> {
                when {
                    mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.status == AsyncTask.Status.FINISHED -> {
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures =
                            DownloadTexturesNRL(mGLView!!.openGLES20Renderer)
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.execute("RAINRATE")
                    }
                    !mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.isCancelled -> {
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.progressDialogShow()
                    }
                }
            }
            ID_MENU_IMAGERY_AIRMASS -> {
                when {
                    mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.status == AsyncTask.Status.FINISHED -> {
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures =
                            DownloadTexturesEumetsat(mGLView!!.openGLES20Renderer)
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.execute("AIRMASS")
                    }
                    !mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.isCancelled -> {
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.progressDialogShow()
                    }
                }
            }
            ID_MENU_IMAGERY_AIRMASS_HD -> {
                when {
                    mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.status == AsyncTask.Status.FINISHED -> {
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures =
                            DownloadTexturesEumetsat(mGLView!!.openGLES20Renderer)
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.execute("AIRMASS_HD")
                    }
                    !mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.isCancelled -> {
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.progressDialogShow()
                    }
                }
            }
            ID_MENU_IMAGERY_MPE_HD -> {
                when {
                    mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.status == AsyncTask.Status.FINISHED -> {
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures =
                            DownloadTexturesEumetsat(mGLView!!.openGLES20Renderer)
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.execute("MPE_HD")
                    }
                    !mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.isCancelled -> {
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.progressDialogShow()
                    }
                }
            }
            ID_MENU_IMAGERY_CCI_CLOUDS -> {
                when {
                    mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.status == AsyncTask.Status.FINISHED -> {
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures =
                            DownloadTexturesCCI(mGLView!!.openGLES20Renderer)
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.execute("CLOUDS")
                    }
                    !mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.isCancelled -> {
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.progressDialogShow()
                    }
                }
            }
            ID_MENU_IMAGERY_CCI_TEMP -> {
                when {
                    mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.status == AsyncTask.Status.FINISHED -> {
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures =
                            DownloadTexturesCCI(mGLView!!.openGLES20Renderer)
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.execute("TEMP")
                    }
                    !mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.isCancelled -> {
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.progressDialogShow()
                    }
                }
            }
            ID_MENU_IMAGERY_CCI_TEMP_AN -> {
                when {
                    mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.status == AsyncTask.Status.FINISHED -> {
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures = DownloadTexturesCCI(mGLView!!.openGLES20Renderer)
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.execute("TEMP_AN")
                    }
                    !mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.isCancelled -> {
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.progressDialogShow()
                    }
                }
            }
            ID_MENU_IMAGERY_CCI_WATER -> {
                when {
                    mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.status == AsyncTask.Status.FINISHED -> {
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures =
                            DownloadTexturesCCI(mGLView!!.openGLES20Renderer)
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.execute("WATER")
                    }
                    !mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.isCancelled -> {
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.progressDialogShow()
                    }
                }
            }
            ID_MENU_IMAGERY_CCI_WIND -> {
                when {
                    mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.status == AsyncTask.Status.FINISHED -> {
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures = DownloadTexturesCCI(mGLView!!.openGLES20Renderer)
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.execute("WIND")
                    }
                    !mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.isCancelled -> {
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.progressDialogShow()
                    }
                }
            }
            ID_MENU_IMAGERY_CCI_JET -> {
                when {
                    mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.status == AsyncTask.Status.FINISHED -> {
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures =
                            DownloadTexturesCCI(mGLView!!.openGLES20Renderer)
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.execute("JET")
                    }
                    !mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.isCancelled -> {
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.progressDialogShow()
                    }
                }
            }
            ID_MENU_IMAGERY_CCI_SNOW -> {
                when {
                    mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.status == AsyncTask.Status.FINISHED -> {
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures =
                            DownloadTexturesCCI(mGLView!!.openGLES20Renderer)
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.execute("SNOW")
                    }
                    !mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.isCancelled -> {
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.progressDialogShow()
                    }
                }
            }
            ID_MENU_IMAGERY_CCI_TEMP_AN_1Y -> {
                when {
                    mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.status == AsyncTask.Status.FINISHED -> {
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures =
                            DownloadTexturesCCI(mGLView!!.openGLES20Renderer)
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.execute("TEMP_AN_1Y")
                    }
                    !mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.isCancelled -> {
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.progressDialogShow()
                    }
                }
            }
            ID_MENU_IMAGERY_CCI_OISST_V2_1Y -> {
                when {
                    mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.status == AsyncTask.Status.FINISHED -> {
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures =
                            DownloadTexturesCCI(mGLView!!.openGLES20Renderer)
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.execute("OISST_V2_1Y")
                    }
                    !mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.isCancelled -> {
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.progressDialogShow()
                    }
                }
            }
            ID_MENU_IMAGERY_CCI_OISST_V2 -> {
                when {
                    mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.status == AsyncTask.Status.FINISHED -> {
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures =
                            DownloadTexturesCCI(mGLView!!.openGLES20Renderer)
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.execute("OISST_V2")
                    }
                    !mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.isCancelled -> {
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.progressDialogShow()
                    }
                }
            }
            ID_MENU_IMAGERY_CCI_ERSST_V5 -> {
                when {
                    mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.status == AsyncTask.Status.FINISHED -> {
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures =
                            DownloadTexturesCCI(mGLView!!.openGLES20Renderer)
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.execute("ERSST_V5")
                    }
                    !mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.isCancelled -> {
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.progressDialogShow()
                    }
                }
            }
            ID_MENU_IMAGERY_INFRARED -> {
                when {
                    mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.status == AsyncTask.Status.FINISHED -> {
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures =
                            DownloadTexturesEumetsat(mGLView!!.openGLES20Renderer)
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.execute("IR108_BW")
                    }
                    !mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.isCancelled -> {
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.progressDialogShow()
                    }
                }
            }
            ID_MENU_IMAGERY_VISIBLE_INFRARED -> {
                when {
                    mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.status == AsyncTask.Status.FINISHED -> {
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures =
                            DownloadTexturesEumetsat(mGLView!!.openGLES20Renderer)
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.execute("VIS006_BW")
                    }
                    !mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.isCancelled -> {
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.progressDialogShow()
                    }
                }
            }
            ID_MENU_IMAGERY_WATER_VAPOR -> {
                when {
                    mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.status == AsyncTask.Status.FINISHED -> {
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures =
                            DownloadTexturesEumetsat(mGLView!!.openGLES20Renderer)
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.execute("WV062_BW")
                    }
                    !mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.isCancelled -> {
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.progressDialogShow()
                    }
                }
            }
            ID_MENU_IMAGERY_MPE -> {
                when {
                    mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.status == AsyncTask.Status.FINISHED -> {
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures =
                            DownloadTexturesEumetsat(mGLView!!.openGLES20Renderer)
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.execute("MPE")
                    }
                    !mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.isCancelled -> {
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.progressDialogShow()
                    }
                }
            }
            ID_MENU_IMAGERY_IODC -> {
                when {
                    mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.status == AsyncTask.Status.FINISHED -> {
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures =
                            DownloadTexturesEumetsat(mGLView!!.openGLES20Renderer)
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.execute("IODC")
                    }
                    !mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.isCancelled -> {
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.progressDialogShow()
                    }
                }
            }
            ID_MENU_IMAGERY_GOES_EAST -> {
                when {
                    mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.status == AsyncTask.Status.FINISHED -> {
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures =
                            DownloadTexturesGoes(mGLView!!.openGLES20Renderer)
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.execute("GOES_EAST")
                    }
                    !mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.isCancelled -> {
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.progressDialogShow()
                    }
                }
            }
            ID_MENU_IMAGERY_GOES_WEST -> {
                when {
                    mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.status == AsyncTask.Status.FINISHED -> {
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures =
                            DownloadTexturesGoes(mGLView!!.openGLES20Renderer)
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.execute("GOES_WEST")
                    }
                    !mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.isCancelled -> {
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.progressDialogShow()
                    }
                }
            }
            ID_MENU_IMAGERY_MTSAT -> {
                when {
                    mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.status == AsyncTask.Status.FINISHED -> {
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures =
                            DownloadTexturesMTSAT(mGLView!!.openGLES20Renderer)
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.execute("MTSAT")
                    }
                    !mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.isCancelled -> {
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.progressDialogShow()
                    }
                }
            }
            ID_MENU_IMAGERY_SSEC_IR -> {
                when {
                    mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.status == AsyncTask.Status.FINISHED -> {
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures =
                            DownloadTexturesSSEC(mGLView!!.openGLES20Renderer)
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.execute("IR")
                    }
                    !mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.isCancelled -> {
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.progressDialogShow()
                    }
                }
            }
            ID_MENU_IMAGERY_SSEC_WATER -> {
                when {
                    mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.status == AsyncTask.Status.FINISHED -> {
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures =
                            DownloadTexturesSSEC(mGLView!!.openGLES20Renderer)
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.execute("WATER")
                    }
                    !mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.isCancelled -> {
                        mGLView!!.openGLES20Renderer!!.mDownloadTextures!!.progressDialogShow()
                    }
                }
            }
        }
        return false
    }
}