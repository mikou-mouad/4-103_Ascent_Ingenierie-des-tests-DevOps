-- Les traversées des 7 prochains jours, recalculées à chaque création de la base
insert into traversee (depart, arrivee, date_heure, navire, capacite)
select trajet.depart, trajet.arrivee,
       date_trunc('day', now()) + (jour || ' days')::interval + trajet.heure,
       trajet.navire, trajet.capacite
from generate_series(1, 7) as jour,
     (values ('Brest',      'Ouessant', interval '8 hours 30 minutes',  'Fromveur II', 280),
             ('Le Conquet', 'Molène',   interval '9 hours 45 minutes',  'Enez Eussa',  120),
             ('Le Conquet', 'Ouessant', interval '17 hours 15 minutes', 'Molenez',      12))
         as trajet (depart, arrivee, heure, navire, capacite);
