package de.rki.coronawarnapp.covidcertificate.recovery.ui.details

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.AppBarLayout
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.bugreporting.ui.toErrorDialogBuilder
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.databinding.FragmentRecoveryCertificateDetailsBinding
import de.rki.coronawarnapp.ui.qrcode.fullscreen.QrCodeFullScreenFragmentArgs
import de.rki.coronawarnapp.ui.view.onOffsetChange
import de.rki.coronawarnapp.util.DialogHelper
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.setUrl
import de.rki.coronawarnapp.util.ui.popBackStack
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

class RecoveryCertificateDetailsFragment : Fragment(R.layout.fragment_recovery_certificate_details), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val binding by viewBinding<FragmentRecoveryCertificateDetailsBinding>()
    private val viewModel: RecoveryCertificateDetailsViewModel by cwaViewModels { viewModelFactory }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        appBarLayout.onOffsetChange { titleAlpha, subtitleAlpha ->
            title.alpha = titleAlpha
            subtitle.alpha = subtitleAlpha
        }

        bindTravelNoticeViews()
        bindToolbar()
        setToolbarOverlay()

        viewModel.qrCode.observe(viewLifecycleOwner) { onQrCodeReady(it) }
        viewModel.errors.observe(viewLifecycleOwner) { onError(it) }
        viewModel.events.observe(viewLifecycleOwner) { onNavEvent(it) }
        viewModel.recoveryCertificate.observe(viewLifecycleOwner) { it?.let { onCertificateReady(it) } }
    }

    private fun FragmentRecoveryCertificateDetailsBinding.onCertificateReady(
        recoveryCertificate: RecoveryCertificate
    ) {
        /* TODO */
    }

    private fun FragmentRecoveryCertificateDetailsBinding.onQrCodeReady(bitmap: Bitmap?) {
        qrCodeCard.apply {
            image.setImageBitmap(bitmap)
            progressBar.hide()
            bitmap?.let { image.setOnClickListener { viewModel.openFullScreen() } }
        }
    }

    private fun FragmentRecoveryCertificateDetailsBinding.onError(error: Throwable) {
        qrCodeCard.progressBar.hide()
        error.toErrorDialogBuilder(requireContext()).show()
    }

    private fun FragmentRecoveryCertificateDetailsBinding.onNavEvent(event: RecoveryCertificateDetailsNavigation) {
        when (event) {
            RecoveryCertificateDetailsNavigation.Back -> popBackStack()
            is RecoveryCertificateDetailsNavigation.FullQrCode -> findNavController().navigate(
                R.id.action_global_qrCodeFullScreenFragment,
                QrCodeFullScreenFragmentArgs(event.qrCodeText).toBundle(),
                null,
                FragmentNavigatorExtras(qrCodeCard.image to qrCodeCard.image.transitionName)
            )
        }
    }

    private fun FragmentRecoveryCertificateDetailsBinding.bindTravelNoticeViews() {
        if (travelNoticeGerman.text ==
            getString(R.string.green_certificate_attribute_certificate_travel_notice_german)
        ) {
            travelNoticeGerman.setUrl(
                R.string.green_certificate_attribute_certificate_travel_notice_german,
                R.string.green_certificate_travel_notice_link_de,
                R.string.green_certificate_travel_notice_link_de
            )
        }

        if (travelNoticeEnglish.text ==
            getString(R.string.green_certificate_attribute_certificate_travel_notice_english)
        ) {
            travelNoticeEnglish.setUrl(
                R.string.green_certificate_attribute_certificate_travel_notice_english,
                R.string.green_certificate_travel_notice_link_en,
                R.string.green_certificate_travel_notice_link_en
            )
        }
    }

    private fun FragmentRecoveryCertificateDetailsBinding.bindToolbar() = toolbar.apply {
        setNavigationOnClickListener { popBackStack() }
        setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_covid_certificate_delete -> {
                    DialogHelper.showDialog(deleteTestConfirmationDialog)
                    true
                }
                else -> onOptionsItemSelected(it)
            }
        }
    }

    private fun setToolbarOverlay() {
        val width = requireContext().resources.displayMetrics.widthPixels
        val params: CoordinatorLayout.LayoutParams = binding.scrollView.layoutParams as (CoordinatorLayout.LayoutParams)

        val textParams = binding.subtitle.layoutParams as (LinearLayout.LayoutParams)
        textParams.bottomMargin = (width / 3) + 170
        binding.subtitle.requestLayout()

        val behavior: AppBarLayout.ScrollingViewBehavior = params.behavior as (AppBarLayout.ScrollingViewBehavior)
        behavior.overlayTop = (width / 3) + 170
    }

    private val deleteTestConfirmationDialog by lazy {
        DialogHelper.DialogInstance(
            requireActivity(),
            R.string.green_certificate_details_dialog_remove_test_title,
            R.string.green_certificate_details_dialog_remove_test_message,
            R.string.green_certificate_details_dialog_remove_test_button_positive,
            R.string.green_certificate_details_dialog_remove_test_button_negative,
            positiveButtonFunction = {
                viewModel.onDeleteTestConfirmed()
            }
        )
    }
}
