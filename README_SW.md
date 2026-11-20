# DATA GUARD 4.0 — Native Android Firewall starter

Huu ni mradi wa chanzo wa Android. GitHub Actions hujenga APK ya majaribio (debug); APK hii haijapimwa kwenye simu halisi.

## Kazi iliyopo
- Orodha ya programu zilizosakinishwa (hutegemea ruhusa na toleo la Android).
- Chagua programu zinazopaswa kuzuiwa.
- Android `VpnService` huunda VPN ya ndani bila VPN server ya nje.
- Pakiti zinazoingia kwenye TUN interface hutupwa; hazipelekwi kwenye intaneti.

## Tahadhari
- Android lazima ionyeshe na mtumiaji akubali ruhusa ya VPN.
- Android huruhusu VPN moja tu kuwa hai kwa wakati mmoja.
- Huu ni msingi wa majaribio. Jaribu kwenye simu halisi; uthibitishaji wa IPv4/IPv6, DNS, programu tofauti na matoleo tofauti ya Android bado unahitajika.
- `QUERY_ALL_PACKAGES` ina masharti ya sera za Google Play.
- Hii si antivirus, si cache cleaner ya mfumo, wala haijumuishi data-usage dashboard au battery controls.
- Firewall hii ni packet-dropper ya msingi: programu zilizochaguliwa hazitapata muunganisho kupitia tunnel. Usitumie kama suluhisho la usalama lililothibitishwa.

## Kujenga APK bila PC
1. Pakia yaliyomo ndani ya ZIP kwenye root ya GitHub repository (si ZIP yenyewe pekee).
2. Hakikisha `.github/workflows/android.yml` ipo.
3. Fungua Actions → Build DATA GUARD APK → Run workflow.
4. Build ikifaulu, fungua run husika na pakua artifact `DATA-GUARD-debug-APK`.
