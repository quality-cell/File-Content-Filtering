# Утилита фильтрации содержимого файлов.

## Описание работы утилиты
Данная утилита отфильтровывает разные типы данных в разные файлы. Целые числа в один
выходной файл, вещественные в другой, строки в третий. По умолчанию файлы с
результатами располагаются в текущей папке с именами integers.txt, floats.txt, strings.txt.

## Пример запуска утилиты
mvn clean package

java -jar util.jar -s -a -p sample- in1.txt in2.txt

## Информация о проекте
- Версия Java - 21
- Версия Maven - 3.9.5
- Библиотека [commons-lang3 - 3.12.0](https://mvnrepository.com/artifact/org.apache.commons/commons-lang3/3.12.0)

## Описание комманд
- -p - Задает префикс для выходных файлов
- -o - Задает путь где будут распологаться выходные файлы
- -a - Задает режим добавления в существующий файл
- -s - Выводит краткую статистику
- -f - Выводит полную статистику
- -out - Задает имя файла, куда будет добавлен текст всех входных файлов


