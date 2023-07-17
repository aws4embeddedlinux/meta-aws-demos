#
# SPDX-License-Identifier: MIT
#

from oeqa.runtime.case import OERuntimeTestCase
from oeqa.core.decorator.depends import OETestDepends
from oeqa.runtime.decorator.package import OEHasPackage

class GreenGrassBinTest(OERuntimeTestCase):

    def setUp(self):
        pass

    def tearDown(self):
        pass

    @OETestDepends(['ssh.SSHTest.test_ssh'])
    @OEHasPackage(['coreutils', 'busybox', 'greengrass-bin'])
    def test_gg_bin_nucleus_launches(self):
        (status, output) = self.target.run('sh /greengrass/v2/alts/current/distro/bin/loader', timeout=120)
        self.logger.info("output: %s", output)
        self.assertIn('Launched Nucleus successfully.', output)
        self.assertEqual(status, 0)

