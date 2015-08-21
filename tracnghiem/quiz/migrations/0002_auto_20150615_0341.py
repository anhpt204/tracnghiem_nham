# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations


class Migration(migrations.Migration):

    dependencies = [
        ('quiz', '0001_initial'),
    ]

    operations = [
        migrations.AlterField(
            model_name='cathi',
            name='tg_bat_dau',
            field=models.TimeField(verbose_name=b'Th\xe1\xbb\x9di gian b\xe1\xba\xaft \xc4\x91\xe1\xba\xa7u'),
        ),
        migrations.AlterField(
            model_name='cathi',
            name='tg_ket_thuc',
            field=models.TimeField(verbose_name=b'Th\xe1\xbb\x9di gian k\xe1\xba\xbft th\xc3\xbac'),
        ),
    ]
