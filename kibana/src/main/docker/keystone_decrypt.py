#
# Copyright (c) 2015 EMC Corporation
# All Rights Reserved
#
# This software contains the intellectual property of EMC Corporation
# or is licensed to EMC Corporation from third parties.  Use of this
# software and the intellectual property contained therein is expressly
# limited to the terms and conditions of the License Agreement under which
# it is provided by or on behalf of EMC.
#

import os
from platform_encryption.aes_util import AESUtil

# instantiate the aesutil object that gives access to the encrypt/decrypt and decrypt the KS_CSA_PWD received and return it
with AESUtil() as au:
    decrypted = au.decrypt(os.environ["KS_CSA_PWD"])
    exit(decrypted)
