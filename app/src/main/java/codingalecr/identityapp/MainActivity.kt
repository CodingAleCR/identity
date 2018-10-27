package codingalecr.identityapp

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_captureId.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(packageManager) != null) {
                IntentIntegrator(this).initiateScan()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.contents == null) {
                toast("Cancelled")
            } else {
                val p = IdentityUtils.parse(result.contents.toByteArray(Charsets.ISO_8859_1))
                tv_id.text = p?.id
                tv_name.text = p?.name
                tv_lastname.text = p?.getLastname()
                tv_gender.text = p?.gender.toString()
                tv_birthdate.text = p?.birthdate
                tv_expirationdate.text = p?.expirationdate

                toast("Scanned: " + p.toString())
            }
        }
    }
}
