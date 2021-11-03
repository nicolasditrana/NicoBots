# NicoBots
Hola gente! 

Este es un proyecto con distintos bots que voy haciendo.
Les dejo mi perfil de LinkedIn por si quieren consultarme algo: https://www.linkedin.com/in/nicoditrana/

El primero es para ir recorriendo los posteos de tu LinkedIn y ayudarte a buscar trabajo, se encuentra en la clase AnalizePostText.java


El bot realiza las siguientes acciones:

- Verifica si el sistema tiene la propiedad webdriver.chrome.driver y sino la setea dependiendo del valor de la propiedad: urlDriver
- Inicia sesión con el usuario y contraseña guardado en el archivo de propiedades: application.properties (propiedades: user y passw)
- Ordena por Más Recientes, si la propiedad: searchByMoreRecent es true
- Valida la propiedad loop para saber cuantos posteos va a analizar 

- Por cada posteo:
  - Muestra el posteo en pantalla
  - Guarda el id único del posteo y si ya lo tenia guardado no lo procesa
  - Verifica que incluya todas las claves de la propiedad: obligatoryKeys (valores separados por ;)
  - Si encuentra todas las claves obligatorias, genera un score, para esto valida los valores de la propiedad: additionalsKeys (separados por ;) y suma un punto por cada coincidencia
  - Luego guarda el nombre del dueño del posteo y el link a su perfil
  - Si la propiedad: addNewPeople es true. Valida si ya tiene al usuario agregado a su lista de contactos y de no ser asi, se conecta/la sigue automaticamente. 
  - Realiza un scroll vertical (lo que permite cargar mas posteos)
  - Analiza si se encuentra el botón Ver nuevas publicaciones (el cual aparece luego de muchas publicaciones y al hacerle click refresca la pagina)
  
- Una vez finalizada la busqueda, genera un Excel con los resultados:
  - Utilizando la propiedad fileLocation para saber en que carpeta guardar el archivo
  - Y la propiedad excelFormatForBrowser para que se vea correctamente desde un browser
  - El Excel tiene las siguientes columnas:
    - Score: Con el puntaje obtenido a partir de las additionalsKeys
    - People: Con el nombre del dueño del posteo
    - State: Con el tipo de conexión que se tiene con el usuario (Connected, Following, Follow sent, Connect sent, Not aggregate)
    - Profile: Con el link al perfil del usuario
    - Post: Con el texto del posteo analizado

Importante: El bot funciona si esta el LinkedIn en Español o en Ingles
