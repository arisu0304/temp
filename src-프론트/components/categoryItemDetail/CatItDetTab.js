import React, { useState } from 'react';
import '../../css/CategoryItemDetail.css';
import { Link } from 'react-scroll'

const CatItDetTab = () => {


  return (
    <ul className="CID-tab">
      <li>
        <Link to="CID-item-info" smooth={true} duration={500} className="CID-tablinks">
        물품정보
        </Link>
      </li>
      <li>
        <Link to="CID-item-inquiry" smooth={true} duration={500} className="CID-tablinks">
          물품 문의
        </Link>
      </li>
      <li>
        <Link to="CID-item-return-info" smooth={true} duration={500} className="CID-tablinks">
          반품 정보
        </Link>
      </li>
    </ul>
  );
};

export default CatItDetTab;
