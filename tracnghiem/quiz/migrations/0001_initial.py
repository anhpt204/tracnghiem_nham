# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations
from django.conf import settings


class Migration(migrations.Migration):

    dependencies = [
        migrations.swappable_dependency(settings.AUTH_USER_MODEL),
    ]

    operations = [
        migrations.CreateModel(
            name='Answer',
            fields=[
                ('id', models.AutoField(verbose_name='ID', serialize=False, auto_created=True, primary_key=True)),
                ('content', models.CharField(max_length=1000, verbose_name=b'Ph\xc6\xb0\xc6\xa1ng \xc3\xa1n tr\xe1\xba\xa3 l\xe1\xbb\x9di')),
                ('is_correct', models.BooleanField(default=False, help_text=b'Ph\xc6\xb0\xc6\xa1ng \xc3\xa1n \xc4\x91\xc3\xbang?', verbose_name=b'L\xc3\xa0 ph\xc6\xb0\xc6\xa1ng \xc3\xa1n \xc4\x91\xc3\xbang')),
            ],
            options={
                'verbose_name': 'Ph\u01b0\u01a1ng \xe1n tr\u1ea3 l\u1eddi',
                'verbose_name_plural': 'Danh s\xe1ch ph\u01b0\u01a1ng \xe1n tr\u1ea3 l\u1eddi',
            },
        ),
        migrations.CreateModel(
            name='CaThi',
            fields=[
                ('id', models.AutoField(verbose_name='ID', serialize=False, auto_created=True, primary_key=True)),
                ('title', models.CharField(max_length=200, verbose_name=b'Ca thi')),
                ('description', models.TextField(null=True, verbose_name=b'Ghi ch\xc3\xba', blank=True)),
                ('ngay_thi', models.DateField(verbose_name=b'Ng\xc3\xa0y thi')),
                ('tg_bat_dau', models.DateTimeField(verbose_name=b'Th\xe1\xbb\x9di gian b\xe1\xba\xaft \xc4\x91\xe1\xba\xa7u')),
                ('tg_ket_thuc', models.DateTimeField(verbose_name=b'Th\xe1\xbb\x9di gian k\xe1\xba\xbft th\xc3\xbac')),
                ('ds_cau_hoi', models.CommaSeparatedIntegerField(max_length=1024, verbose_name=b'Danh sach cau hoi (ids)')),
                ('tao_moi_de_thi', models.BooleanField(default=True, verbose_name=b'T\xe1\xba\xa1o m\xe1\xbb\x9bi \xc4\x91\xe1\xbb\x81 thi cho c\xc3\xa1c sinh vi\xc3\xaan')),
                ('random_order', models.BooleanField(default=True, verbose_name=b'Hi\xe1\xbb\x83n th\xe1\xbb\x8b c\xc3\xa2u h\xe1\xbb\x8fi ng\xe1\xba\xabu nhi\xc3\xaan')),
                ('answers_at_end', models.BooleanField(default=False, verbose_name=b'Hi\xe1\xbb\x83n th\xe1\xbb\x8b c\xc3\xa2u tr\xe1\xba\xa3 l\xe1\xbb\x9di khi k\xe1\xba\xbft th\xc3\xbac')),
                ('result_at_end', models.BooleanField(default=True, verbose_name=b'Hi\xe1\xbb\x83n th\xe1\xbb\x8b k\xe1\xba\xbft qu\xe1\xba\xa3 khi k\xe1\xba\xbft th\xc3\xbac')),
                ('exam_paper', models.BooleanField(default=True, verbose_name=b'L\xc6\xb0u b\xc3\xa0i thi')),
                ('single_attempt', models.BooleanField(default=True, verbose_name=b'M\xe1\xbb\x97i ng\xc6\xb0\xe1\xbb\x9di m\xe1\xbb\x99t \xc4\x91\xe1\xbb\x81 thi')),
                ('pass_mark', models.PositiveIntegerField(verbose_name=b'\xc4\x90i\xe1\xbb\x83m \xc4\x91\xe1\xba\xa1t y\xc3\xaau c\xe1\xba\xa7u')),
                ('success_text', models.TextField(verbose_name=b'Th\xc3\xb4ng b\xc3\xa1o \xc4\x91\xc6\xb0\xe1\xbb\xa3c hi\xe1\xbb\x83n th\xe1\xbb\x8b n\xe1\xba\xbfu th\xc3\xad sinh v\xc6\xb0\xe1\xbb\xa3t qua', blank=True)),
                ('fail_text', models.TextField(verbose_name=b'Th\xc3\xb4ng b\xc3\xa1o \xc4\x91\xc6\xb0\xe1\xbb\xa3c hi\xe1\xbb\x83n th\xe1\xbb\x8b n\xe1\xba\xbfu th\xc3\xad sinh kh\xc3\xb4ng v\xc6\xb0\xe1\xbb\xa3t qua', blank=True)),
                ('draft', models.BooleanField(default=False, verbose_name=b'B\xe1\xba\xa3n nh\xc3\xa1p')),
            ],
            options={
                'verbose_name': 'Ca thi',
                'verbose_name_plural': 'Danh s\xe1ch ca thi',
            },
        ),
        migrations.CreateModel(
            name='CaThi_Setting',
            fields=[
                ('id', models.AutoField(verbose_name='ID', serialize=False, auto_created=True, primary_key=True)),
                ('question_type', models.CharField(default=b'MC', max_length=5, verbose_name=b'Lo\xe1\xba\xa1i c\xc3\xa2u h\xe1\xbb\x8fi', choices=[(b'TF', b'C\xc3\xa2u h\xe1\xbb\x8fi \xc4\x90\xc3\xbang - Sai'), (b'MC', b'C\xc3\xa2u h\xe1\xbb\x8fi Multiple Choice'), (b'ESSAY', b'C\xc3\xa2u h\xe1\xbb\x8fi t\xe1\xbb\xb1 lu\xe1\xba\xadn')])),
                ('mark_per_question', models.PositiveIntegerField(default=1, verbose_name=b'\xc4\x90i\xe1\xbb\x83m cho m\xe1\xbb\x97i c\xc3\xa2u h\xe1\xbb\x8fi')),
                ('num_of_questions', models.PositiveIntegerField(default=1, verbose_name=b's\xe1\xbb\x91 c\xc3\xa2u h\xe1\xbb\x8fi')),
                ('ca_thi', models.ForeignKey(verbose_name=b'Ca thi', to='quiz.CaThi')),
            ],
            options={
                'verbose_name': 'C\u1ea5u h\xecnh ca thi',
                'verbose_name_plural': 'C\u1ea5u h\xecnh ca thi',
            },
        ),
        migrations.CreateModel(
            name='DeThi',
            fields=[
                ('id', models.AutoField(verbose_name='ID', serialize=False, auto_created=True, primary_key=True)),
                ('ds_cau_hoi', models.TextField(default={}, verbose_name=b'Danh sach cau hoi')),
                ('user_answers', models.TextField(default={}, verbose_name=b'Danh sach cau tra loi cua thi sinh', blank=True)),
                ('complete', models.BooleanField(default=False, verbose_name=b'Da hoan thanh bai thi chua?')),
                ('start', models.DateTimeField(auto_now_add=True, verbose_name=b'Bat dau luc')),
                ('end', models.DateTimeField(null=True, verbose_name=b'Ket thuc luc', blank=True)),
                ('diem', models.PositiveIntegerField(default=0, verbose_name=b'Diem thi')),
                ('ca_thi', models.ForeignKey(verbose_name=b'Ca thi', to='quiz.CaThi')),
            ],
        ),
        migrations.CreateModel(
            name='Diem',
            fields=[
                ('id', models.AutoField(verbose_name='ID', serialize=False, auto_created=True, primary_key=True)),
                ('diem', models.PositiveIntegerField(verbose_name=b'\xc4\x90i\xe1\xbb\x83m')),
            ],
            options={
                'verbose_name': '\u0110i\u1ec3m',
                'verbose_name_plural': 'B\u1ea3ng \u0111i\u1ec3m',
            },
        ),
        migrations.CreateModel(
            name='DMLop',
            fields=[
                ('id', models.AutoField(verbose_name='ID', serialize=False, auto_created=True, primary_key=True)),
                ('ma_lop', models.CharField(unique=True, max_length=5, verbose_name=b'M\xc3\xa3 l\xe1\xbb\x9bp')),
                ('ten_lop', models.CharField(unique=True, max_length=200, verbose_name=b'L\xe1\xbb\x9bp')),
                ('si_so', models.PositiveIntegerField(null=True, verbose_name=b'S\xc4\xa9 s\xe1\xbb\x91', blank=True)),
            ],
            options={
                'verbose_name': 'L\u1edbp',
                'verbose_name_plural': 'Danh s\xe1ch l\u1edbp',
            },
        ),
        migrations.CreateModel(
            name='DMMonThi',
            fields=[
                ('id', models.AutoField(verbose_name='ID', serialize=False, auto_created=True, primary_key=True)),
                ('ma_mon_thi', models.CharField(unique=True, max_length=10, verbose_name=b'M\xc3\xa3 m\xc3\xb4n thi')),
                ('ten_mon_thi', models.CharField(unique=True, max_length=200, verbose_name=b'M\xc3\xb4n thi')),
            ],
            options={
                'verbose_name': 'M\xf4n thi',
                'verbose_name_plural': 'Danh s\xe1ch m\xf4n thi',
            },
        ),
        migrations.CreateModel(
            name='DMSinhVien',
            fields=[
                ('id', models.AutoField(verbose_name='ID', serialize=False, auto_created=True, primary_key=True)),
                ('ma_sv', models.PositiveIntegerField(unique=True, verbose_name=b'M\xc3\xa3 sinh vi\xc3\xaan')),
                ('ho_ten', models.CharField(max_length=50, verbose_name=b'H\xe1\xbb\x8d v\xc3\xa0 t\xc3\xaan')),
                ('lop', models.ForeignKey(verbose_name=b'L\xe1\xbb\x9bp', to='quiz.DMLop')),
                ('user', models.OneToOneField(to=settings.AUTH_USER_MODEL)),
            ],
            options={
                'verbose_name': 'Sinh vi\xean',
                'verbose_name_plural': 'Danh s\xe1ch sinh vi\xean',
            },
        ),
        migrations.CreateModel(
            name='Question',
            fields=[
                ('id', models.AutoField(verbose_name='ID', serialize=False, auto_created=True, primary_key=True)),
                ('question_type', models.CharField(default=b'MC', max_length=5, verbose_name=b'Lo\xe1\xba\xa1i c\xc3\xa2u h\xe1\xbb\x8fi', choices=[(b'TF', b'C\xc3\xa2u h\xe1\xbb\x8fi \xc4\x90\xc3\xbang - Sai'), (b'MC', b'C\xc3\xa2u h\xe1\xbb\x8fi Multiple Choice'), (b'ESSAY', b'C\xc3\xa2u h\xe1\xbb\x8fi t\xe1\xbb\xb1 lu\xe1\xba\xadn')])),
                ('figure', models.ImageField(upload_to=b'uploads/%Y/%m/%d', null=True, verbose_name=b'\xe1\xba\xa2nh', blank=True)),
                ('content', models.TextField(max_length=1000, verbose_name=b'C\xc3\xa2u h\xe1\xbb\x8fi')),
            ],
            options={
                'verbose_name': 'C\xe2u h\u1ecfi',
                'verbose_name_plural': 'Danh s\xe1ch c\xe2u h\u1ecfi',
            },
        ),
        migrations.CreateModel(
            name='QuestionGroup',
            fields=[
                ('id', models.AutoField(verbose_name='ID', serialize=False, auto_created=True, primary_key=True)),
                ('name', models.CharField(unique=True, max_length=50, verbose_name=b'Nh\xc3\xb3m c\xc3\xa2u h\xe1\xbb\x8fi')),
                ('description', models.TextField(null=True, verbose_name=b'Ghi ch\xc3\xba', blank=True)),
            ],
            options={
                'verbose_name': 'Nh\xf3m c\xe2u h\u1ecfi',
                'verbose_name_plural': 'Danh s\xe1ch nh\xf3m c\xe2u h\u1ecfi',
            },
        ),
        migrations.CreateModel(
            name='EssayQuestion',
            fields=[
                ('question_ptr', models.OneToOneField(parent_link=True, auto_created=True, primary_key=True, serialize=False, to='quiz.Question')),
                ('answer', models.TextField(verbose_name=b'Tr\xe1\xba\xa3 l\xe1\xbb\x9di')),
            ],
            options={
                'verbose_name': 'C\xe2u h\u1ecfi t\u1ef1 lu\u1eadn',
                'verbose_name_plural': 'Danh s\xe1ch c\xe2u h\u1ecfi t\u1ef1 lu\u1eadn',
            },
            bases=('quiz.question',),
        ),
        migrations.CreateModel(
            name='MCQuestion',
            fields=[
                ('question_ptr', models.OneToOneField(parent_link=True, auto_created=True, primary_key=True, serialize=False, to='quiz.Question')),
                ('answer_order', models.CharField(blank=True, max_length=30, null=True, verbose_name=b'Th\xe1\xbb\xa9 t\xe1\xbb\xb1 hi\xe1\xbb\x83n th\xe1\xbb\x8b c\xc3\xa2u tr\xe1\xba\xa3 l\xe1\xbb\x9di', choices=[(b'CONTENT', b'N\xe1\xbb\x99i dung'), (b'RANDOM', b'Ng\xe1\xba\xabu nhi\xc3\xaan'), (b'NONE', b'None')])),
            ],
            options={
                'verbose_name': 'C\xe2u h\u1ecfi lo\u1ea1i Multiple choice',
                'verbose_name_plural': 'Danh s\xe1ch c\xe2u h\u1ecfi lo\u1ea1i Multiple choice',
            },
            bases=('quiz.question',),
        ),
        migrations.CreateModel(
            name='TFQuestion',
            fields=[
                ('question_ptr', models.OneToOneField(parent_link=True, auto_created=True, primary_key=True, serialize=False, to='quiz.Question')),
                ('is_correct', models.BooleanField(default=False, verbose_name=b'L\xc3\xa0 \xc4\x91\xc3\xa1p \xc3\xa1n \xc4\x91\xc3\xbang?')),
            ],
            options={
                'ordering': ['mon_thi'],
                'verbose_name': 'C\xe2u h\u1ecfi \u0110\xfang/Sai',
                'verbose_name_plural': 'Danh s\xe1ch c\xe2u h\u1ecfi \u0110\xfang/Sai',
            },
            bases=('quiz.question',),
        ),
        migrations.AddField(
            model_name='question',
            name='ca_thi',
            field=models.ManyToManyField(to='quiz.CaThi', verbose_name=b'Ca thi', blank=True),
        ),
        migrations.AddField(
            model_name='question',
            name='mon_thi',
            field=models.ForeignKey(verbose_name=b'M\xc3\xb4n thi', to='quiz.DMMonThi'),
        ),
        migrations.AddField(
            model_name='question',
            name='question_group',
            field=models.ForeignKey(verbose_name=b'Nh\xc3\xb3m c\xc3\xa2u h\xe1\xbb\x8fi', to='quiz.QuestionGroup'),
        ),
        migrations.AddField(
            model_name='diem',
            name='mon_thi',
            field=models.ForeignKey(verbose_name=b'M\xc3\xb4n thi', to='quiz.DMMonThi'),
        ),
        migrations.AddField(
            model_name='diem',
            name='sinh_vien',
            field=models.ForeignKey(verbose_name=b'Sinh vi\xc3\xaan', to='quiz.DMSinhVien'),
        ),
        migrations.AddField(
            model_name='dethi',
            name='sinh_vien',
            field=models.ForeignKey(verbose_name=b'Sinh Vi\xc3\xaan', to='quiz.DMSinhVien'),
        ),
        migrations.AddField(
            model_name='cathi_setting',
            name='question_group',
            field=models.ForeignKey(verbose_name=b'Nh\xc3\xb3m c\xc3\xa2u h\xe1\xbb\x8fi', to='quiz.QuestionGroup'),
        ),
        migrations.AddField(
            model_name='cathi',
            name='lop_thi',
            field=models.ForeignKey(verbose_name=b'L\xe1\xbb\x9bp thi', to='quiz.DMLop'),
        ),
        migrations.AddField(
            model_name='cathi',
            name='mon_thi',
            field=models.ForeignKey(verbose_name=b'M\xc3\xb4n thi', to='quiz.DMMonThi'),
        ),
        migrations.AddField(
            model_name='answer',
            name='question',
            field=models.ForeignKey(verbose_name=b'C\xc3\xa2u h\xe1\xbb\x8fi', to='quiz.MCQuestion'),
        ),
    ]
