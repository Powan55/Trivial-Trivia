insert into categories (name)
select 'Sport' where not exists (select 1 from categories where name = 'Sport')
and exists (select 1 from questions original
            where (original.text = 'What is the capital of France?' and original.option_1 = 'London' and original.option_2 = 'Berlin' and original.option_3 = 'Madrid' and original.option_4 = 'Paris' and original.answer = 'Paris' and original.category_id = (select id from categories where name = 'Geography') and original.difficulty = 'EASY'))
and exists (select 1 from questions original
            where (original.text = 'Which planet is known as the Red Planet?' and original.option_1 = 'Venus' and original.option_2 = 'Jupiter' and original.option_3 = 'Mars' and original.option_4 = 'Saturn' and original.answer = 'Mars' and original.category_id = (select id from categories where name = 'Science') and original.difficulty = 'EASY'))
and exists (select 1 from questions original
            where (original.text = 'Who is the author of "Romeo and Juliet"?' and original.option_1 = 'Jane Austen' and original.option_2 = 'Charles Dickens' and original.option_3 = 'Mark Twain' and original.option_4 = 'William Shakespeare' and original.answer = 'William Shakespeare' and original.category_id = (select id from categories where name = 'Arts and entertainment') and original.difficulty = 'EASY'))
and exists (select 1 from questions original
            where (original.text = 'Which gas do plants absorb from the atmosphere?' and original.option_1 = 'Oxygen (O2)' and original.option_2 = 'Nitrogen (N2)' and original.option_3 = 'Carbon dioxide (CO2)' and original.option_4 = 'Hydrogen (H2)' and original.answer = 'Carbon dioxide (CO2)' and original.category_id = (select id from categories where name = 'Science') and original.difficulty = 'EASY'))
and exists (select 1 from questions original
            where (original.text = 'What is the largest mammal in the world?' and original.option_1 = 'African Elephant' and original.option_2 = 'Giraffe' and original.option_3 = 'Blue Whale' and original.option_4 = 'Lion' and original.answer = 'Blue Whale' and original.category_id = (select id from categories where name = 'Science') and original.difficulty = 'EASY'))
and exists (select 1 from questions original
            where (original.text = 'In which year did Christopher Columbus first arrive in the Americas?' and original.option_1 = '1492' and original.option_2 = '1776' and original.option_3 = '1066' and original.option_4 = '1969' and original.answer = '1492' and original.category_id = (select id from categories where name = 'History') and original.difficulty = 'EASY'))
and exists (select 1 from questions original
            where (original.text = 'What is the chemical symbol for gold?' and original.option_1 = 'Ag' and original.option_2 = 'Ge' and original.option_3 = 'Au' and original.option_4 = 'Fe' and original.answer = 'Au' and original.category_id = (select id from categories where name = 'Science') and original.difficulty = 'EASY'))
and exists (select 1 from questions original
            where (original.text = 'Who painted the Mona Lisa?' and original.option_1 = 'Vincent van Gogh' and original.option_2 = 'Pablo Picasso' and original.option_3 = 'Michelangelo' and original.option_4 = 'Leonardo da Vinci' and original.answer = 'Leonardo da Vinci' and original.category_id = (select id from categories where name = 'Arts and entertainment') and original.difficulty = 'EASY'))
and exists (select 1 from questions original
            where (original.text = 'What is the largest organ in the human body?' and original.option_1 = 'Heart' and original.option_2 = 'Brain' and original.option_3 = 'Skin' and original.option_4 = 'Liver' and original.answer = 'Skin' and original.category_id = (select id from categories where name = 'Science') and original.difficulty = 'EASY'))
and exists (select 1 from questions original
            where (original.text = 'Which gas do humans exhale when they breathe out?' and original.option_1 = 'Oxygen (O2)' and original.option_2 = 'Nitrogen (N2)' and original.option_3 = 'Carbon dioxide (CO2)' and original.option_4 = 'Hydrogen (H2)' and original.answer = 'Carbon dioxide (CO2)' and original.category_id = (select id from categories where name = 'Science') and original.difficulty = 'EASY'));

insert into questions (text, option_1, option_2, option_3, option_4, answer, category_id, difficulty)
select bank.text, bank.option_1, bank.option_2, bank.option_3, bank.option_4, bank.answer, category.id, bank.difficulty
from (values
    ('Which ocean basin has the greatest surface area?', 'Atlantic Ocean', 'Indian Ocean', 'Arctic Ocean', 'Pacific Ocean', 'Pacific Ocean', 'Geography', 'EASY'),
    ('In which country is Machu Picchu?', 'Peru', 'Bolivia', 'Ecuador', 'Chile', 'Peru', 'Geography', 'EASY'),
    ('In which Indian city is the Taj Mahal?', 'Jaipur', 'Agra', 'Delhi', 'Mumbai', 'Agra', 'Geography', 'MEDIUM'),
    ('Which mountain has the highest summit above sea level?', 'K2', 'Mount Everest', 'Kangchenjunga', 'Lhotse', 'Mount Everest', 'Geography', 'EASY'),
    ('Which continent is surrounded by the Southern Ocean?', 'Australia', 'Africa', 'Antarctica', 'South America', 'Antarctica', 'Geography', 'MEDIUM'),
    ('Which lake is the deepest in the world?', 'Lake Baikal', 'Lake Victoria', 'Lake Superior', 'Lake Tanganyika', 'Lake Baikal', 'Geography', 'HARD'),
    ('On which river''s bank does the Taj Mahal stand?', 'Ganges', 'Yamuna', 'Indus', 'Godavari', 'Yamuna', 'Geography', 'HARD'),
    ('In which year was the Statue of Liberty dedicated?', '1886', '1776', '1815', '1924', '1886', 'History', 'MEDIUM'),
    ('People from which country gave the Statue of Liberty to the United States?', 'Italy', 'Spain', 'France', 'Germany', 'France', 'History', 'EASY'),
    ('Which Mughal emperor commissioned the Taj Mahal?', 'Akbar', 'Shah Jahan', 'Aurangzeb', 'Babur', 'Shah Jahan', 'History', 'MEDIUM'),
    ('In which year did the Continental Congress adopt the Declaration of Independence?', '1781', '1787', '1791', '1776', '1776', 'History', 'EASY'),
    ('In which year was the United States Constitution signed?', '1776', '1781', '1787', '1791', '1787', 'History', 'MEDIUM'),
    ('How many amendments form the United States Bill of Rights?', 'Seven', 'Twelve', 'Fifteen', 'Ten', 'Ten', 'History', 'HARD'),
    ('Which Egyptian pharaoh''s Great Pyramid is the surviving ancient wonder at Giza?', 'Khufu', 'Ramesses II', 'Djoser', 'Tutankhamun', 'Khufu', 'History', 'HARD'),
    ('Which planet is known as the Red Planet?', 'Venus', 'Mars', 'Jupiter', 'Saturn', 'Mars', 'Science', 'EASY'),
    ('What is the chemical symbol for gold?', 'Ag', 'Ge', 'Au', 'Fe', 'Au', 'Science', 'EASY'),
    ('What is the largest mammal in the world?', 'African Elephant', 'Giraffe', 'Lion', 'Blue Whale', 'Blue Whale', 'Science', 'EASY'),
    ('Which planet is the smallest of the eight planets in our solar system?', 'Venus', 'Mars', 'Neptune', 'Mercury', 'Mercury', 'Science', 'MEDIUM'),
    ('Which moon is the largest in our solar system?', 'Ganymede', 'Titan', 'Europa', 'Callisto', 'Ganymede', 'Science', 'MEDIUM'),
    ('Which planet has the shortest rotation period in our solar system?', 'Earth', 'Saturn', 'Jupiter', 'Neptune', 'Jupiter', 'Science', 'HARD'),
    ('Rusting of which element gives the Red Planet its reddish appearance?', 'Copper', 'Sulfur', 'Aluminium', 'Iron', 'Iron', 'Science', 'HARD'),
    ('Who is the author of "Romeo and Juliet"?', 'William Shakespeare', 'Jane Austen', 'Charles Dickens', 'Mark Twain', 'William Shakespeare', 'Arts and entertainment', 'EASY'),
    ('Who painted the Mona Lisa?', 'Vincent van Gogh', 'Leonardo da Vinci', 'Pablo Picasso', 'Michelangelo', 'Leonardo da Vinci', 'Arts and entertainment', 'EASY'),
    ('Who painted The Wedding Feast at Cana displayed in the Louvre?', 'Titian', 'Tintoretto', 'Veronese', 'Caravaggio', 'Veronese', 'Arts and entertainment', 'MEDIUM'),
    ('Who composed the opera Carmen?', 'Giuseppe Verdi', 'Giacomo Puccini', 'Gioachino Rossini', 'Georges Bizet', 'Georges Bizet', 'Arts and entertainment', 'MEDIUM'),
    ('Which film won the Animated Feature Film Oscar at the 2003 Academy Awards?', 'Spirited Away', 'Ice Age', 'Lilo & Stitch', 'Treasure Planet', 'Spirited Away', 'Arts and entertainment', 'MEDIUM'),
    ('Which forest''s branches conceal Malcolm''s army in Macbeth?', 'Forest of Arden', 'Birnam Wood', 'Sherwood Forest', 'Epping Forest', 'Birnam Wood', 'Arts and entertainment', 'HARD'),
    ('Who shared the Costume Design Oscar for Gandhi with John Mollo?', 'Eiko Ishioka', 'Colleen Atwood', 'Bhanu Athaiya', 'Sandy Powell', 'Bhanu Athaiya', 'Arts and entertainment', 'HARD'),
    ('Which city hosted the first modern Olympic Games in 1896?', 'Athens', 'Paris', 'London', 'Rome', 'Athens', 'Sport', 'EASY'),
    ('Under standard association football rules what is the maximum number of players per team on the field?', 'Nine', 'Eleven', 'Ten', 'Twelve', 'Eleven', 'Sport', 'EASY'),
    ('How many minutes does each standard half of an association football match last before added time?', '30', '40', '45', '50', '45', 'Sport', 'EASY'),
    ('What is the maximum normal half-time interval in association football?', 'Five minutes', 'Ten minutes', 'Fifteen minutes', 'Twenty minutes', 'Fifteen minutes', 'Sport', 'MEDIUM'),
    ('How wide is a regulation association football goal between the inside edges of its posts?', '6.32 metres', '7.32 metres', '8.32 metres', '9.32 metres', '7.32 metres', 'Sport', 'HARD'),
    ('How high is the lower edge of an association football goal''s crossbar above the ground?', '2.14 metres', '2.24 metres', '2.44 metres', '2.34 metres', '2.44 metres', 'Sport', 'HARD'),
    ('How many sports featured at the first modern Olympic Games in 1896?', 'Seven', 'Eleven', 'Thirteen', 'Nine', 'Nine', 'Sport', 'HARD'),
    ('What is the capital of France?', 'London', 'Berlin', 'Paris', 'Madrid', 'Paris', 'Geography', 'EASY'),
    ('Off which country''s northeast coast lies the Great Barrier Reef?', 'Indonesia', 'New Zealand', 'South Africa', 'Australia', 'Australia', 'Geography', 'EASY'),
    ('In which country is Angkor Wat?', 'Cambodia', 'Thailand', 'Laos', 'Myanmar', 'Cambodia', 'Geography', 'EASY'),
    ('The Galápagos Islands belong to which country?', 'Peru', 'Ecuador', 'Colombia', 'Chile', 'Ecuador', 'Geography', 'EASY'),
    ('Which mountain is the highest in Africa?', 'Mount Kenya', 'Mount Stanley', 'Mount Meru', 'Kilimanjaro', 'Kilimanjaro', 'Geography', 'MEDIUM'),
    ('Which river flows through the Grand Canyon?', 'Colorado River', 'Mississippi River', 'Columbia River', 'Rio Grande', 'Colorado River', 'Geography', 'MEDIUM'),
    ('Iguazú Falls straddles the border between which two countries?', 'Chile and Argentina', 'Argentina and Brazil', 'Brazil and Uruguay', 'Paraguay and Bolivia', 'Argentina and Brazil', 'Geography', 'MEDIUM'),
    ('In which country is the ancient city of Petra?', 'Egypt', 'Lebanon', 'Jordan', 'Türkiye', 'Jordan', 'Geography', 'MEDIUM'),
    ('In which country is Ha Long Bay?', 'Thailand', 'Malaysia', 'Cambodia', 'Viet Nam', 'Viet Nam', 'Geography', 'MEDIUM'),
    ('Which river flows over Victoria Falls?', 'Nile', 'Congo', 'Zambezi', 'Niger', 'Zambezi', 'Geography', 'HARD'),
    ('Victoria Falls lies on the border between Zambia and which country?', 'Botswana', 'Namibia', 'Mozambique', 'Zimbabwe', 'Zimbabwe', 'Geography', 'HARD'),
    ('The annual Serengeti wildebeest migration spans Tanzania and which neighboring country?', 'Kenya', 'Uganda', 'Rwanda', 'Zambia', 'Kenya', 'Geography', 'HARD'),
    ('Which mountain is Antarctica''s highest?', 'Mount Erebus', 'Mount Vinson', 'Mount Sidley', 'Mount Kirkpatrick', 'Mount Vinson', 'Geography', 'HARD'),
    ('In which region of Russia is Lake Baikal?', 'The Caucasus', 'Karelia', 'Siberia', 'The Kola Peninsula', 'Siberia', 'Geography', 'HARD'),
    ('In which year did Christopher Columbus first arrive in the Americas?', '1492', '1776', '1066', '1969', '1492', 'History', 'EASY'),
    ('Who became South Africa''s first democratically elected president in 1994?', 'Desmond Tutu', 'Nelson Mandela', 'Oliver Tambo', 'Thabo Mbeki', 'Nelson Mandela', 'History', 'EASY'),
    ('Which brothers made successful controlled powered flights at Kitty Hawk in 1903?', 'Montgolfier brothers', 'Lumière brothers', 'Wright brothers', 'Grimm brothers', 'Wright brothers', 'History', 'EASY'),
    ('In which year did Apollo 11 first land humans on the Moon?', '1957', '1961', '1965', '1969', '1969', 'History', 'EASY'),
    ('Who was the first woman to receive a Nobel Prize?', 'Marie Curie', 'Lise Meitner', 'Rosalind Franklin', 'Dorothy Hodgkin', 'Marie Curie', 'History', 'EASY'),
    ('In which year did the Berlin Wall fall?', '1979', '1991', '1961', '1989', '1989', 'History', 'MEDIUM'),
    ('In which year was the Suez Canal inaugurated?', '1869', '1815', '1848', '1914', '1869', 'History', 'MEDIUM'),
    ('In which year did French soldiers find the Rosetta Stone?', '1759', '1799', '1776', '1822', '1799', 'History', 'MEDIUM'),
    ('Which English king agreed to Magna Carta at Runnymede?', 'Henry VIII', 'Edward III', 'John', 'Richard III', 'John', 'History', 'MEDIUM'),
    ('In which year was Magna Carta first agreed at Runnymede?', '1066', '1215', '1348', '1485', '1215', 'History', 'HARD'),
    ('Alongside Egyptian hieroglyphs and Demotic which script appears on the Rosetta Stone?', 'Latin', 'Cuneiform', 'Ancient Greek', 'Phoenician', 'Ancient Greek', 'History', 'HARD'),
    ('What was the name of Apollo 11''s lunar module?', 'Columbia', 'Orion', 'Challenger', 'Eagle', 'Eagle', 'History', 'HARD'),
    ('The Meiji Restoration began in which year?', '1868', '1853', '1889', '1905', '1868', 'History', 'HARD'),
    ('Angkor was a major center of which empire?', 'Mughal Empire', 'Khmer Empire', 'Ottoman Empire', 'Maurya Empire', 'Khmer Empire', 'History', 'HARD'),
    ('What is the largest organ in the human body?', 'Skin', 'Heart', 'Brain', 'Liver', 'Skin', 'Science', 'EASY'),
    ('Which gas do plants take in from the atmosphere during photosynthesis?', 'Oxygen (O2)', 'Carbon dioxide (CO2)', 'Nitrogen (N2)', 'Hydrogen (H2)', 'Carbon dioxide (CO2)', 'Science', 'EASY'),
    ('Which waste gas produced by body cells is carried to the lungs and breathed out?', 'Oxygen (O2)', 'Nitrogen (N2)', 'Carbon dioxide (CO2)', 'Hydrogen (H2)', 'Carbon dioxide (CO2)', 'Science', 'EASY'),
    ('Which planet is the largest in our solar system?', 'Saturn', 'Neptune', 'Earth', 'Jupiter', 'Jupiter', 'Science', 'EASY'),
    ('Which protein in red blood cells picks up oxygen in the lungs?', 'Collagen', 'Hemoglobin', 'Keratin', 'Insulin', 'Hemoglobin', 'Science', 'MEDIUM'),
    ('What is the name of the outermost main layer of human skin?', 'Dermis', 'Hypodermis', 'Epidermis', 'Cartilage', 'Epidermis', 'Science', 'MEDIUM'),
    ('What shape is formed by the two linked strands of a DNA molecule?', 'Single ring', 'Flat sheet', 'Straight chain', 'Double helix', 'Double helix', 'Science', 'MEDIUM'),
    ('How many types of nucleotide base normally make up DNA?', 'Four', 'Two', 'Three', 'Six', 'Four', 'Science', 'MEDIUM'),
    ('Dextrose is another name for the D-form of which sugar?', 'Lactose', 'Glucose', 'Sucrose', 'Fructose', 'Glucose', 'Science', 'MEDIUM'),
    ('Which elementary particles are found inside protons and neutrons?', 'Quarks', 'Photons', 'Electrons', 'Neutrinos', 'Quarks', 'Science', 'HARD'),
    ('In DNA adenine normally pairs with which base?', 'Cytosine', 'Thymine', 'Guanine', 'Uracil', 'Thymine', 'Science', 'HARD'),
    ('Which pigment made by human skin helps protect against the sun''s harmful rays?', 'Hemoglobin', 'Chlorophyll', 'Melanin', 'Keratin', 'Melanin', 'Science', 'HARD'),
    ('Which vitamin can human skin make when exposed to sunlight?', 'Vitamin A', 'Vitamin C', 'Vitamin K', 'Vitamin D', 'Vitamin D', 'Science', 'HARD'),
    ('Which skin layer directly beneath the epidermis contains blood vessels and sweat glands?', 'Dermis', 'Epidermis', 'Stratum corneum', 'Stratum basale', 'Dermis', 'Science', 'HARD'),
    ('Who created the Japanese print commonly called The Great Wave?', 'Hiroshige', 'Utamaro', 'Katsushika Hokusai', 'Sesshū', 'Katsushika Hokusai', 'Arts and entertainment', 'EASY'),
    ('Who painted The Starry Night?', 'Claude Monet', 'Paul Cézanne', 'Edvard Munch', 'Vincent van Gogh', 'Vincent van Gogh', 'Arts and entertainment', 'EASY'),
    ('Who painted The Persistence of Memory?', 'Salvador Dalí', 'René Magritte', 'Joan Miró', 'Max Ernst', 'Salvador Dalí', 'Arts and entertainment', 'EASY'),
    ('Which architect designed the Sydney Opera House?', 'Alvar Aalto', 'Jørn Utzon', 'Frank Lloyd Wright', 'Oscar Niemeyer', 'Jørn Utzon', 'Arts and entertainment', 'EASY'),
    ('Which was Walt Disney''s first feature-length animated film?', 'Pinocchio', 'Fantasia', 'Snow White and the Seven Dwarfs', 'Dumbo', 'Snow White and the Seven Dwarfs', 'Arts and entertainment', 'EASY'),
    ('Who directed the 1954 film Seven Samurai?', 'Yasujirō Ozu', 'Akira Kurosawa', 'Kenji Mizoguchi', 'Masaki Kobayashi', 'Akira Kurosawa', 'Arts and entertainment', 'MEDIUM'),
    ('In which year was Jane Austen''s Pride and Prejudice first published?', '1798', '1825', '1813', '1847', '1813', 'Arts and entertainment', 'MEDIUM'),
    ('In which year did the Sydney Opera House open?', '1953', '1963', '1983', '1973', '1973', 'Arts and entertainment', 'MEDIUM'),
    ('Who was the sculptor of the Statue of Liberty?', 'Frédéric Auguste Bartholdi', 'Auguste Rodin', 'Gian Lorenzo Bernini', 'Antonio Canova', 'Frédéric Auguste Bartholdi', 'Arts and entertainment', 'MEDIUM'),
    ('Whose poem is used in the choral finale of Beethoven''s Ninth Symphony?', 'Johann Wolfgang von Goethe', 'Heinrich Heine', 'William Wordsworth', 'Friedrich Schiller', 'Friedrich Schiller', 'Arts and entertainment', 'HARD'),
    ('Which Tagore poetry collection is also known in English as Song Offerings?', 'Gitanjali', 'The Gardener', 'Fruit-Gathering', 'The Fugitive', 'Gitanjali', 'Arts and entertainment', 'HARD'),
    ('The print commonly called The Great Wave belongs to which print series?', 'One Hundred Famous Views of Edo', 'Thirty-six Views of Mount Fuji', 'Fifty-three Stations of the Tōkaidō', 'Eight Views of Ōmi', 'Thirty-six Views of Mount Fuji', 'Arts and entertainment', 'HARD'),
    ('What was the name of the painter known as Veronese?', 'Jacopo Robusti', 'Tiziano Vecellio', 'Paolo Caliari', 'Giovanni Bellini', 'Paolo Caliari', 'Arts and entertainment', 'HARD'),
    ('What is the title of Gabriel García Márquez''s 1982 Nobel Lecture?', 'The Craft of Fiction', 'The Republic of Letters', 'The Art of Memory', 'The Solitude of Latin America', 'The Solitude of Latin America', 'Arts and entertainment', 'HARD'),
    ('In tennis scoring what does love mean?', 'One point', 'Two points', 'Three points', 'Zero points', 'Zero points', 'Sport', 'EASY'),
    ('How many events make up an athletics decathlon?', 'Ten', 'Seven', 'Eight', 'Twelve', 'Ten', 'Sport', 'EASY'),
    ('How many events make up an athletics heptathlon?', 'Five', 'Seven', 'Six', 'Eight', 'Seven', 'Sport', 'EASY'),
    ('Under FIBA basketball rules how many players from each team normally play on court at once?', 'Six', 'Seven', 'Five', 'Four', 'Five', 'Sport', 'EASY'),
    ('In standard advantage-scoring tennis what is a score of 40-all called?', 'Love', 'Break point', 'Set point', 'Deuce', 'Deuce', 'Sport', 'MEDIUM'),
    ('In tennis which shot is intended to pass high over an opponent near the net?', 'Lob', 'Slice', 'Volley', 'Drop shot', 'Lob', 'Sport', 'MEDIUM'),
    ('How many points is a successful free throw worth in standard basketball?', 'Two', 'One', 'Three', 'Four', 'One', 'Sport', 'MEDIUM'),
    ('Under FIBA rules how many quarters make up regulation basketball play?', 'Two', 'Three', 'Four', 'Five', 'Four', 'Sport', 'MEDIUM'),
    ('What is the official marathon distance in kilometres?', '40.000', '41.195', '43.000', '42.195', '42.195', 'Sport', 'MEDIUM'),
    ('How many players from each team must start on court in FIBA 3x3 basketball?', 'Three', 'Two', 'Four', 'Five', 'Three', 'Sport', 'MEDIUM'),
    ('In which year was the official marathon distance standardized?', '1921', '1896', '1908', '1912', '1921', 'Sport', 'HARD'),
    ('How many seconds does a team have to attempt a shot in FIBA 3x3 basketball?', '14', '12', '20', '24', '12', 'Sport', 'HARD'),
    ('In FIBA 3x3 basketball how many points is a successful shot from outside the arc worth?', 'One', 'Three', 'Two', 'Four', 'Two', 'Sport', 'HARD'),
    ('What are the dimensions of a doubles beach tennis court?', '18 by 9 metres', '20 by 10 metres', '14 by 7 metres', '16 by 8 metres', '16 by 8 metres', 'Sport', 'HARD')) as bank (text, option_1, option_2, option_3, option_4, answer, category, difficulty)
join categories category on category.name = bank.category
where exists (select 1 from questions original
            where (original.text = 'What is the capital of France?' and original.option_1 = 'London' and original.option_2 = 'Berlin' and original.option_3 = 'Madrid' and original.option_4 = 'Paris' and original.answer = 'Paris' and original.category_id = (select id from categories where name = 'Geography') and original.difficulty = 'EASY'))
and exists (select 1 from questions original
            where (original.text = 'Which planet is known as the Red Planet?' and original.option_1 = 'Venus' and original.option_2 = 'Jupiter' and original.option_3 = 'Mars' and original.option_4 = 'Saturn' and original.answer = 'Mars' and original.category_id = (select id from categories where name = 'Science') and original.difficulty = 'EASY'))
and exists (select 1 from questions original
            where (original.text = 'Who is the author of "Romeo and Juliet"?' and original.option_1 = 'Jane Austen' and original.option_2 = 'Charles Dickens' and original.option_3 = 'Mark Twain' and original.option_4 = 'William Shakespeare' and original.answer = 'William Shakespeare' and original.category_id = (select id from categories where name = 'Arts and entertainment') and original.difficulty = 'EASY'))
and exists (select 1 from questions original
            where (original.text = 'Which gas do plants absorb from the atmosphere?' and original.option_1 = 'Oxygen (O2)' and original.option_2 = 'Nitrogen (N2)' and original.option_3 = 'Carbon dioxide (CO2)' and original.option_4 = 'Hydrogen (H2)' and original.answer = 'Carbon dioxide (CO2)' and original.category_id = (select id from categories where name = 'Science') and original.difficulty = 'EASY'))
and exists (select 1 from questions original
            where (original.text = 'What is the largest mammal in the world?' and original.option_1 = 'African Elephant' and original.option_2 = 'Giraffe' and original.option_3 = 'Blue Whale' and original.option_4 = 'Lion' and original.answer = 'Blue Whale' and original.category_id = (select id from categories where name = 'Science') and original.difficulty = 'EASY'))
and exists (select 1 from questions original
            where (original.text = 'In which year did Christopher Columbus first arrive in the Americas?' and original.option_1 = '1492' and original.option_2 = '1776' and original.option_3 = '1066' and original.option_4 = '1969' and original.answer = '1492' and original.category_id = (select id from categories where name = 'History') and original.difficulty = 'EASY'))
and exists (select 1 from questions original
            where (original.text = 'What is the chemical symbol for gold?' and original.option_1 = 'Ag' and original.option_2 = 'Ge' and original.option_3 = 'Au' and original.option_4 = 'Fe' and original.answer = 'Au' and original.category_id = (select id from categories where name = 'Science') and original.difficulty = 'EASY'))
and exists (select 1 from questions original
            where (original.text = 'Who painted the Mona Lisa?' and original.option_1 = 'Vincent van Gogh' and original.option_2 = 'Pablo Picasso' and original.option_3 = 'Michelangelo' and original.option_4 = 'Leonardo da Vinci' and original.answer = 'Leonardo da Vinci' and original.category_id = (select id from categories where name = 'Arts and entertainment') and original.difficulty = 'EASY'))
and exists (select 1 from questions original
            where (original.text = 'What is the largest organ in the human body?' and original.option_1 = 'Heart' and original.option_2 = 'Brain' and original.option_3 = 'Skin' and original.option_4 = 'Liver' and original.answer = 'Skin' and original.category_id = (select id from categories where name = 'Science') and original.difficulty = 'EASY'))
and exists (select 1 from questions original
            where (original.text = 'Which gas do humans exhale when they breathe out?' and original.option_1 = 'Oxygen (O2)' and original.option_2 = 'Nitrogen (N2)' and original.option_3 = 'Carbon dioxide (CO2)' and original.option_4 = 'Hydrogen (H2)' and original.answer = 'Carbon dioxide (CO2)' and original.category_id = (select id from categories where name = 'Science') and original.difficulty = 'EASY'))
and not exists (select 1 from questions existing
                where existing.text = bank.text and existing.option_1 = bank.option_1 and existing.option_2 = bank.option_2
                  and existing.option_3 = bank.option_3 and existing.option_4 = bank.option_4 and existing.answer = bank.answer
                  and existing.category_id = category.id and existing.difficulty = bank.difficulty);

delete from questions q
where exists (select 1 from questions original
            where (original.text = 'What is the capital of France?' and original.option_1 = 'London' and original.option_2 = 'Berlin' and original.option_3 = 'Madrid' and original.option_4 = 'Paris' and original.answer = 'Paris' and original.category_id = (select id from categories where name = 'Geography') and original.difficulty = 'EASY'))
and exists (select 1 from questions original
            where (original.text = 'Which planet is known as the Red Planet?' and original.option_1 = 'Venus' and original.option_2 = 'Jupiter' and original.option_3 = 'Mars' and original.option_4 = 'Saturn' and original.answer = 'Mars' and original.category_id = (select id from categories where name = 'Science') and original.difficulty = 'EASY'))
and exists (select 1 from questions original
            where (original.text = 'Who is the author of "Romeo and Juliet"?' and original.option_1 = 'Jane Austen' and original.option_2 = 'Charles Dickens' and original.option_3 = 'Mark Twain' and original.option_4 = 'William Shakespeare' and original.answer = 'William Shakespeare' and original.category_id = (select id from categories where name = 'Arts and entertainment') and original.difficulty = 'EASY'))
and exists (select 1 from questions original
            where (original.text = 'Which gas do plants absorb from the atmosphere?' and original.option_1 = 'Oxygen (O2)' and original.option_2 = 'Nitrogen (N2)' and original.option_3 = 'Carbon dioxide (CO2)' and original.option_4 = 'Hydrogen (H2)' and original.answer = 'Carbon dioxide (CO2)' and original.category_id = (select id from categories where name = 'Science') and original.difficulty = 'EASY'))
and exists (select 1 from questions original
            where (original.text = 'What is the largest mammal in the world?' and original.option_1 = 'African Elephant' and original.option_2 = 'Giraffe' and original.option_3 = 'Blue Whale' and original.option_4 = 'Lion' and original.answer = 'Blue Whale' and original.category_id = (select id from categories where name = 'Science') and original.difficulty = 'EASY'))
and exists (select 1 from questions original
            where (original.text = 'In which year did Christopher Columbus first arrive in the Americas?' and original.option_1 = '1492' and original.option_2 = '1776' and original.option_3 = '1066' and original.option_4 = '1969' and original.answer = '1492' and original.category_id = (select id from categories where name = 'History') and original.difficulty = 'EASY'))
and exists (select 1 from questions original
            where (original.text = 'What is the chemical symbol for gold?' and original.option_1 = 'Ag' and original.option_2 = 'Ge' and original.option_3 = 'Au' and original.option_4 = 'Fe' and original.answer = 'Au' and original.category_id = (select id from categories where name = 'Science') and original.difficulty = 'EASY'))
and exists (select 1 from questions original
            where (original.text = 'Who painted the Mona Lisa?' and original.option_1 = 'Vincent van Gogh' and original.option_2 = 'Pablo Picasso' and original.option_3 = 'Michelangelo' and original.option_4 = 'Leonardo da Vinci' and original.answer = 'Leonardo da Vinci' and original.category_id = (select id from categories where name = 'Arts and entertainment') and original.difficulty = 'EASY'))
and exists (select 1 from questions original
            where (original.text = 'What is the largest organ in the human body?' and original.option_1 = 'Heart' and original.option_2 = 'Brain' and original.option_3 = 'Skin' and original.option_4 = 'Liver' and original.answer = 'Skin' and original.category_id = (select id from categories where name = 'Science') and original.difficulty = 'EASY'))
and exists (select 1 from questions original
            where (original.text = 'Which gas do humans exhale when they breathe out?' and original.option_1 = 'Oxygen (O2)' and original.option_2 = 'Nitrogen (N2)' and original.option_3 = 'Carbon dioxide (CO2)' and original.option_4 = 'Hydrogen (H2)' and original.answer = 'Carbon dioxide (CO2)' and original.category_id = (select id from categories where name = 'Science') and original.difficulty = 'EASY'))
and ((q.text = 'What is the capital of France?' and q.option_1 = 'London' and q.option_2 = 'Berlin' and q.option_3 = 'Madrid' and q.option_4 = 'Paris' and q.answer = 'Paris' and q.category_id = (select id from categories where name = 'Geography') and q.difficulty = 'EASY')
  or (q.text = 'Which planet is known as the Red Planet?' and q.option_1 = 'Venus' and q.option_2 = 'Jupiter' and q.option_3 = 'Mars' and q.option_4 = 'Saturn' and q.answer = 'Mars' and q.category_id = (select id from categories where name = 'Science') and q.difficulty = 'EASY')
  or (q.text = 'Who is the author of "Romeo and Juliet"?' and q.option_1 = 'Jane Austen' and q.option_2 = 'Charles Dickens' and q.option_3 = 'Mark Twain' and q.option_4 = 'William Shakespeare' and q.answer = 'William Shakespeare' and q.category_id = (select id from categories where name = 'Arts and entertainment') and q.difficulty = 'EASY')
  or (q.text = 'Which gas do plants absorb from the atmosphere?' and q.option_1 = 'Oxygen (O2)' and q.option_2 = 'Nitrogen (N2)' and q.option_3 = 'Carbon dioxide (CO2)' and q.option_4 = 'Hydrogen (H2)' and q.answer = 'Carbon dioxide (CO2)' and q.category_id = (select id from categories where name = 'Science') and q.difficulty = 'EASY')
  or (q.text = 'What is the largest mammal in the world?' and q.option_1 = 'African Elephant' and q.option_2 = 'Giraffe' and q.option_3 = 'Blue Whale' and q.option_4 = 'Lion' and q.answer = 'Blue Whale' and q.category_id = (select id from categories where name = 'Science') and q.difficulty = 'EASY')
  or (q.text = 'Who painted the Mona Lisa?' and q.option_1 = 'Vincent van Gogh' and q.option_2 = 'Pablo Picasso' and q.option_3 = 'Michelangelo' and q.option_4 = 'Leonardo da Vinci' and q.answer = 'Leonardo da Vinci' and q.category_id = (select id from categories where name = 'Arts and entertainment') and q.difficulty = 'EASY')
  or (q.text = 'What is the largest organ in the human body?' and q.option_1 = 'Heart' and q.option_2 = 'Brain' and q.option_3 = 'Skin' and q.option_4 = 'Liver' and q.answer = 'Skin' and q.category_id = (select id from categories where name = 'Science') and q.difficulty = 'EASY')
  or (q.text = 'Which gas do humans exhale when they breathe out?' and q.option_1 = 'Oxygen (O2)' and q.option_2 = 'Nitrogen (N2)' and q.option_3 = 'Carbon dioxide (CO2)' and q.option_4 = 'Hydrogen (H2)' and q.answer = 'Carbon dioxide (CO2)' and q.category_id = (select id from categories where name = 'Science') and q.difficulty = 'EASY'));
