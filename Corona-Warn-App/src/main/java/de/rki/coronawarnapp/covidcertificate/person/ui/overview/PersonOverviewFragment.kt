package de.rki.coronawarnapp.covidcertificate.person.ui.overview

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.CameraPermissionCard
import de.rki.coronawarnapp.covidcertificate.person.ui.overview.items.CertificatesItem
import de.rki.coronawarnapp.databinding.PersonOverviewFragmentBinding
import de.rki.coronawarnapp.util.ExternalActionHelper.openAppDetailsSettings
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.lists.decorations.TopBottomPaddingDecorator
import de.rki.coronawarnapp.util.lists.diffutil.update
import de.rki.coronawarnapp.util.onScroll
import de.rki.coronawarnapp.util.ui.doNavigate
import de.rki.coronawarnapp.util.ui.viewBinding
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import javax.inject.Inject

// Shows a list of multiple persons
class PersonOverviewFragment : Fragment(R.layout.person_overview_fragment), AutoInject {
    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val viewModel: PersonOverviewViewModel by cwaViewModels { viewModelFactory }
    private val binding by viewBinding<PersonOverviewFragmentBinding>()
    private val personOverviewAdapter = PersonOverviewAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.apply {
            bindToolbar()
            bindRecycler()
            scanQrcodeFab.setOnClickListener { viewModel.onScanQrCode() }
        }
        viewModel.personCertificates.observe(viewLifecycleOwner) { binding.bindViews(it) }
        viewModel.events.observe(viewLifecycleOwner) { onNavEvent(it) }
    }

    private fun onNavEvent(event: PersonOverviewFragmentEvents) {
        when (event) {
            is OpenPersonDetailsFragment -> doNavigate(
                PersonOverviewFragmentDirections.actionPersonOverviewFragmentToVaccinationListFragment(
                    event.personIdentifier
                )
            )
            is ShowDeleteDialog -> MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.test_certificate_delete_dialog_title)
                .setMessage(R.string.test_certificate_delete_dialog_body)
                .setNegativeButton(R.string.test_certificate_delete_dialog_cancel_button) { _, _ -> }
                .setCancelable(false)
                .setPositiveButton(R.string.test_certificate_delete_dialog_confirm_button) { _, _ ->
                    viewModel.deleteTestCertificate(event.certificateId)
                }
                .show()

            is ShowRefreshErrorDialog -> MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.test_certificate_refresh_dialog_title)
                .setMessage(event.error.localizedMessage ?: getString(R.string.errors_generic_headline))
                .setCancelable(false)
                .setPositiveButton(R.string.test_certificate_refresh_dialog_confirm_button) { _, _ -> }
                .show()

            ScanQrCode -> Toast.makeText(
                requireContext(),
                "TODO \uD83D\uDEA7 Tomorrow maybe?!",
                Toast.LENGTH_LONG
            ).show()
            OpenAppDeviceSettings -> openAppDetailsSettings()
        }
    }

    private fun PersonOverviewFragmentBinding.bindToolbar() {
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_information -> doNavigate(
                    PersonOverviewFragmentDirections.actionPersonOverviewFragmentToVaccinationConsentFragment(false)
                ).run { true }

                else -> onOptionsItemSelected(it)
            }
        }
    }

    private fun PersonOverviewFragmentBinding.bindViews(items: List<CertificatesItem>) {
        scanQrcodeFab.isGone = items.any { it is CameraPermissionCard.Item }
        emptyLayout.isVisible = items.isEmpty()
        personOverviewAdapter.update(items)
    }

    private fun PersonOverviewFragmentBinding.bindRecycler() = recyclerView.apply {
        adapter = personOverviewAdapter
        addItemDecoration(TopBottomPaddingDecorator(topPadding = R.dimen.spacing_tiny))
        itemAnimator = DefaultItemAnimator()

        with(scanQrcodeFab) { onScroll { extend -> if (extend) extend() else shrink() } }
    }
}
