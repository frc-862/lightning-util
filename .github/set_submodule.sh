#!/bin/bash

git status

git rev-parse HEAD

echo git rev-parse HEAD >> $THUNDER_HEAD

echo -e "\033[1;31mLAO GAN MA SCRIPT DONE!!\033[0m"